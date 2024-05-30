package com.example.myapplication.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.DeletePost;
import com.example.myapplication.DAL.InsertComentario;
import com.example.myapplication.DAL.InsertLike;
import com.example.myapplication.DAL.ShowComments;
import com.example.myapplication.Metodos;
import com.example.myapplication.Modelo.Comentario;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class PublicacionesAdapter extends RecyclerView.Adapter<PublicacionesAdapter.PublicacionViewHolder> {
    private List<Publicacion> publicaciones;
    private Context context;

    ConfigIp configIp = new ConfigIp();

    private Boolean esPerfil;
    boolean esLike = false;

    public PublicacionesAdapter(Context context, List<Publicacion> publicaciones, Boolean esPerfil) {
        this.context = context;
        this.publicaciones = publicaciones;
        this.esPerfil = esPerfil;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion_detallada, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicacion publicacion = publicaciones.get(position);

        // Usar directamente las imágenes de la publicación
        if (!publicacion.getImagenes().isEmpty()) {
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(publicacion.getImagenes());
            holder.viewPagerImages.setAdapter(imageSliderAdapter);
        }


        for (int i = 0; i < publicaciones.get(position).getPerfilesLikes().size() && !esLike; i++) {
            String id = publicaciones.get(position).getPerfilesLikes().get(i);
            if (id.equals(Metodos.idPerfilMascota)) {
                esLike = true;
            } else {
                esLike = false;
            }
        }

        if (esLike) {
            holder.heart.setImageResource(R.drawable.heart_solid);
        } else {
            holder.heart.setImageResource(R.drawable.heart_regular);
        }

        holder.heart.setOnClickListener(v -> {
            InsertLike insertLike = new InsertLike();
            String url = "http://" + configIp.IP + ":8000/api/dar_like/";
            insertLike.likear(url, context, publicacion.getId(), response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    String likes = jsonObject.getString("likes");
                    if (message.equals("Like added")) {
                        holder.heart.setImageResource(R.drawable.heart_solid);
                        holder.likesTextView.setText(String.format("%d likes", publicacion.setLikes(Integer.parseInt(likes))));
                    } else {
                        holder.heart.setImageResource(R.drawable.heart_regular);
                        holder.likesTextView.setText(String.format("%d likes", publicacion.setLikes(Integer.parseInt(likes))));
                    }

                } catch (JSONException e) {
                    Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                }


            }, error -> {
                Log.e("Error likear", "Error server: " + error.getMessage());
            });
        });

        holder.likesTextView.setText(String.format("%d likes", publicacion.getLikes()));
        holder.descriptionTextView.setText(publicacion.getDescripcion());
        holder.descriptionTextView.setVisibility(publicacion.getDescripcion().isEmpty() ? View.GONE : View.VISIBLE);

        // Configuración del RecyclerView de comentarios
        List<Comentario> comentarios = publicacion.getComentarios();
        ComentariosAdapter comentariosAdapter = new ComentariosAdapter(context, comentarios);
        holder.recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewComentarios.setAdapter(comentariosAdapter);

        holder.verComentariosTextView.setVisibility(comentarios.isEmpty() ? View.GONE : View.VISIBLE);
        holder.recyclerViewComentarios.setVisibility(View.GONE);
        holder.editTextComentario.setVisibility(comentarios.isEmpty() ? View.VISIBLE : View.GONE);
        holder.buttonEnviarComentario.setVisibility(comentarios.isEmpty() ? View.VISIBLE : View.GONE);
        holder.verComentariosTextView.setOnClickListener(v -> {
            // Alternar visibilidad
            boolean isVisible = holder.recyclerViewComentarios.getVisibility() == View.VISIBLE;
            holder.recyclerViewComentarios.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.editTextComentario.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.buttonEnviarComentario.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.verComentariosTextView.setText(isVisible ? "Ver comentarios" : "Ocultar comentarios");
        });

        holder.buttonEnviarComentario.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            String idMascota = prefs.getString("last_selected_mascota_id", "");
            String comentarioText = holder.editTextComentario.getText().toString();
            if (!comentarioText.isEmpty()) {
                Comentario nuevoComentario = new Comentario(comentarioText, publicacion.getId(), idMascota);
                InsertComentario insertComentario = new InsertComentario();
                String url = "http://" + configIp.IP + ":8000/api/insert_comentario/";
                insertComentario.insertComentario(url, context, nuevoComentario, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        holder.editTextComentario.setText("");
                        ShowComments showComments = new ShowComments();
                        String url = "http://" + configIp.IP + ":8000/api/comentarios/" + publicacion.getId() + "/";
                        showComments.obtenerComentarios(url, context, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Gson gson = new Gson();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONArray jsonArray = jsonObject.getJSONArray("comentarios");
                                    Type listType = new TypeToken<List<Comentario>>() {
                                    }.getType();
                                    List<Comentario> nuevosComentarios = gson.fromJson(jsonArray.toString(), listType);
                                    comentariosAdapter.setComentarios(nuevosComentarios);
                                    comentariosAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    Log.e("JSONError", "Error parsing JSON", e);
                                }


                            }
                        });

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error comentario", error.getMessage());

                    }

                });
            }
        });

        if (esPerfil) {
            holder.buttonDeletePost.setVisibility(View.VISIBLE);
            holder.buttonDeletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAbsoluteAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        // Crear y mostrar AlertDialog
                        new AlertDialog.Builder(context)
                                .setTitle("Confirmar Eliminación") // Título del diálogo
                                .setMessage("¿Estás seguro de que deseas eliminar esta publicación?") // Mensaje de confirmación
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continuar con la eliminación si el usuario confirma
                                        DeletePost deletePost = new DeletePost();
                                        String url = "http://" + configIp.IP + ":8000/api/eliminar_publicacion/";
                                        deletePost.eliminarPublicacion(url, context, publicaciones.get(adapterPosition), new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONObject jsonResponse = new JSONObject(response);
                                                    String message = jsonResponse.getString("message");
                                                    if (message.equals("1")) {
                                                        publicaciones.remove(adapterPosition);
                                                        notifyItemRemoved(adapterPosition);
                                                        notifyItemRangeChanged(adapterPosition, publicaciones.size());

                                                        Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "La publicación no se pudo eliminar", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                        }, error -> Toast.makeText(context, "Error al eliminar la publicación", Toast.LENGTH_SHORT).show());
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null) // No hacer nada en caso de "No"
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPagerImages;
        ImageView heart;
        TextView likesTextView, descriptionTextView, verComentariosTextView;
        RecyclerView recyclerViewComentarios;
        EditText editTextComentario;
        Button buttonEnviarComentario;
        ImageButton buttonDeletePost;

        public PublicacionViewHolder(View itemView) {
            super(itemView);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            heart = itemView.findViewById(R.id.heartImageView);
            likesTextView = itemView.findViewById(R.id.text_likes);
            descriptionTextView = itemView.findViewById(R.id.text_description);
            recyclerViewComentarios = itemView.findViewById(R.id.recyclerViewComentarios);
            verComentariosTextView = itemView.findViewById(R.id.text_ver_comentarios);
            editTextComentario = itemView.findViewById(R.id.editTextComentario);
            buttonEnviarComentario = itemView.findViewById(R.id.buttonEnviarComentario);
            buttonDeletePost = itemView.findViewById(R.id.buttonDeletePost);
        }
    }
}
