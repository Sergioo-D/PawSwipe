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
import androidx.fragment.app.FragmentActivity;
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
import com.example.myapplication.vista.Fragmentos.ComentariosBottomSheetDialogFragment;
import com.example.myapplication.vista.Fragmentos.FragmentHome;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.PublicacionViewHolder> {
    private List<Publicacion> publicaciones;
    private Context context;

    ConfigIp configIp = new ConfigIp();

    boolean esLike = false;

    public FeedAdapter(Context context, List<Publicacion> publicaciones) {
        this.context = context;
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicaciones_fit, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicacion publicacion = publicaciones.get(position);

        // Usar directamente las imágenes de la publicación
        if (!publicacion.getImagenes().isEmpty()) {
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(publicacion.getImagenes());
            holder.pagerImagesMuro.setAdapter(imageSliderAdapter);
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
            holder.botonLikeMuro.setImageResource(R.drawable.heart_solid);
        } else {
            holder.botonLikeMuro.setImageResource(R.drawable.heart_regular);
        }

        holder.botonLikeMuro.setOnClickListener(v -> {
            InsertLike insertLike = new InsertLike();
            String url = "http://" + configIp.IP + ":8000/api/dar_like/";
            insertLike.likear(url, context, publicacion.getId(), response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    String likes = jsonObject.getString("likes");
                    if (message.equals("Like added")) {
                        holder.botonLikeMuro.setImageResource(R.drawable.heart_solid);
                        holder.textoLikeMuro.setText(String.format("%d likes", publicacion.setLikes(Integer.parseInt(likes))));
                    } else {
                        holder.botonLikeMuro.setImageResource(R.drawable.heart_regular);
                        holder.textoLikeMuro.setText(String.format("%d likes", publicacion.setLikes(Integer.parseInt(likes))));
                    }

                } catch (JSONException e) {
                    Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                }


            }, error -> {
                Log.e("Error likear", "Error server: " + error.getMessage());
            });
        });
        holder.nombreMuro.setText(publicacion.getNombreMascota());
        Picasso.get()
                .load("http://" + configIp.IP + ":8000" + publicacion.getFotoPerfil())
                .fit()
                .centerInside()
                .into(holder.fotoPerfilMuro);
        holder.fechaPublicacion.setText(publicacion.getFechaPublicacion());
        holder.textoLikeMuro.setText(String.format("%d likes", publicacion.getLikes()));

        // Configuración del RecyclerView de comentarios
        List<Comentario> comentarios = publicacion.getComentarios();
        //ComentariosAdapter comentariosAdapter = new ComentariosAdapter(context, comentarios);

        holder.textoVerComentariosMuro.setVisibility(comentarios.isEmpty() ? View.GONE : View.VISIBLE);
        holder.textoVerComentariosMuro.setOnClickListener(v -> {

            ComentariosBottomSheetDialogFragment.show((FragmentActivity) v.getContext(), comentarios, publicacion);
            // Alternar visibilidad
           /* boolean isVisible = holder.recyclerViewComentariosMuro.getVisibility() == View.VISIBLE;
            holder.recyclerViewComentariosMuro.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.editTextComentario.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.buttonEnviarComentario.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.textoVerComentariosMuro.setText(isVisible ? "Ver comentarios" : "Ocultar comentarios");*/
        });

        /*holder.buttonEnviarComentario.setOnClickListener(v -> {
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
        });*/

    }


    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {

        ImageView fotoPerfilMuro;

        TextView nombreMuro;

        TextView fechaPublicacion;
        ViewPager2 pagerImagesMuro;

        ImageButton botonLikeMuro;

        TextView textoLikeMuro;

        TextView textoVerComentariosMuro;


        public PublicacionViewHolder(View itemView) {
            super(itemView);
            nombreMuro = itemView.findViewById(R.id.textViewNombreMuro);
            fotoPerfilMuro = itemView.findViewById(R.id.fotoPerfilMuro);
            fechaPublicacion = itemView.findViewById(R.id.textViewFecha);
            pagerImagesMuro = itemView.findViewById(R.id.viewPagerImagesMuro);
            botonLikeMuro = itemView.findViewById(R.id.botonLikeMuro);
            textoLikeMuro = itemView.findViewById(R.id.textoLikeMuro);
            textoVerComentariosMuro = itemView.findViewById(R.id.textoVerComentariosMuro);
        }
    }
}
