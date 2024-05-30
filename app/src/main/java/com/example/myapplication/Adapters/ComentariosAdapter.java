package com.example.myapplication.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.DeleteComentario;
import com.example.myapplication.DAL.DeletePost;
import com.example.myapplication.Modelo.Comentario;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder> {
    private List<Comentario> comentarios;
    private Context context;

    private final ConfigIp configIp = new ConfigIp();


    public ComentariosAdapter(Context context, List<Comentario> comentarios) {
        this.context = context;
        this.comentarios = (comentarios != null) ? comentarios : new ArrayList<>();
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        Picasso.get().load("http://" + configIp.IP + ":8000" + comentario.getPerfilInfo().getFotoPerfil())
                .placeholder(R.drawable.paw_solid)
                .into(holder.imagenAutor);
        holder.autorTextView.setText(comentario.getPerfilInfo().getNombreMascota());
        holder.comentarioTextView.setText(comentario.getTexto());
        holder.fechaTextView.setText(comentario.getFechaRelativa());

        holder.botonEliminar.setOnClickListener(new View.OnClickListener() {

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
                                    DeleteComentario deleteComentario = new DeleteComentario();
                                    String url = "http://" + configIp.IP + ":8000/api/eliminar_comentario/";
                                    deleteComentario.eliminarComentario(url, context, comentarios.get(adapterPosition), new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject jsonResponse = new JSONObject(response);
                                                String message = jsonResponse.getString("message");
                                                if (message.equals("1")) {
                                                    comentarios.remove(adapterPosition);
                                                    notifyItemRemoved(adapterPosition);
                                                    notifyItemRangeChanged(adapterPosition, comentarios.size());

                                                    Toast.makeText(context, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "El comentario no se pudo eliminar", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    }, error -> Toast.makeText(context, "Error al eliminar el comentario", Toast.LENGTH_SHORT).show());
                                }
                            })
                            .setNegativeButton(android.R.string.no, null) // No hacer nada en caso de "No"
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (comentarios != null) ? comentarios.size() : 0;
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView autorTextView, comentarioTextView, fechaTextView;
        ImageView imagenAutor;

        ImageView botonEliminar;

        public ComentarioViewHolder(View itemView) {
            super(itemView);
            autorTextView = itemView.findViewById(R.id.textViewAutor);
            comentarioTextView = itemView.findViewById(R.id.textViewComentario);
            fechaTextView = itemView.findViewById(R.id.textViewFecha);
            imagenAutor = itemView.findViewById(R.id.imagenAutor);
            botonEliminar = itemView.findViewById(R.id.buttonClose);
        }
    }
}
