package com.example.myapplication.Adapters;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import static com.example.myapplication.vista.Fragmentos.FragmentoPerfil.REQUEST_CODE_PUBLICACION;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ConfigIp;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;
import com.example.myapplication.vista.Activities.ActivityPublicacion;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfil;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfilVisitado;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {
    private List<Publicacion> publicaciones;
    private Context context;

    private Boolean esFragmentPerfil;

    ConfigIp configIp = new ConfigIp();

    private  FragmentoPerfil fragmentoPerfil;
    private FragmentoPerfilVisitado fragmentoPerfilVisitado;

    public PublicacionAdapter(Context context, List<Publicacion> publicaciones, FragmentoPerfil fragmentoPerfil) {
        this.context = context;
        this.publicaciones = publicaciones;
        this.fragmentoPerfil = fragmentoPerfil;
        this.esFragmentPerfil = true;
    }

    public PublicacionAdapter(Context context, List<Publicacion> publicaciones, FragmentoPerfilVisitado fragmentoPerfilVisitado) {
        this.context = context;
        this.publicaciones = publicaciones;
        this.fragmentoPerfilVisitado = fragmentoPerfilVisitado;
        this.esFragmentPerfil = false;
    }


    @Override
    public PublicacionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PublicacionViewHolder holder, int position) {
        Publicacion publicacion = publicaciones.get(position);
        if (!publicacion.getImagenes().isEmpty()) {
            Picasso.get()
                    .load("http://" + configIp.IP + ":8000" + publicacion.getImagenes().get(0).getUrlImagen())
                    .placeholder(R.drawable.logoapk)
                    .fit()
                    .into(holder.imagenView);
            if (publicacion.getImagenes().size() > 1) {
                holder.iconoView.setVisibility(View.VISIBLE);
            } else {
                holder.iconoView.setVisibility(View.GONE);
            }

        }

        holder.itemView.setOnClickListener(v -> {
            if (esFragmentPerfil) {
                Intent intent = new Intent(context, ActivityPublicacion.class);
                intent.putExtra("publicaciones", (Serializable) publicaciones);
                intent.putExtra("position", position);
                intent.putExtra("esPerfil", true);
                fragmentoPerfil.startActivityForResult(intent, FragmentoPerfil.REQUEST_CODE_PUBLICACION);
            } else {
                Intent intent = new Intent(context, ActivityPublicacion.class);
                intent.putExtra("publicaciones", (Serializable) publicaciones);
                intent.putExtra("position", position);
                intent.putExtra("esPerfil", false);
                fragmentoPerfilVisitado.startActivity(intent);
            }

            //fragmentoPerfil.startActivity(intent);
           // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenView;

        ImageView iconoView;

        public PublicacionViewHolder(View itemView) {
            super(itemView);
            imagenView = itemView.findViewById(R.id.imagen);
            iconoView = itemView.findViewById(R.id.icono);
        }
    }
}
