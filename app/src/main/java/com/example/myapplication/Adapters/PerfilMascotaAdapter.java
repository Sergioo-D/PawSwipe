package com.example.myapplication.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.ShowPerfilVisitado;
import com.example.myapplication.Metodos;
import com.example.myapplication.Modelo.Buscador;
import com.example.myapplication.Modelo.PerfilCompletoResponse;
import com.example.myapplication.R;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfilVisitado;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PerfilMascotaAdapter extends RecyclerView.Adapter<PerfilMascotaAdapter.PerfilViewHolder> {

    private List<Buscador> listaPerfilesBuscador;

    private ConfigIp configIp = new ConfigIp();


    public static class PerfilViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mascotaImage;
        private final TextView mascotaName;

        public PerfilViewHolder(View itemView) {
            super(itemView);
            mascotaImage = itemView.findViewById(R.id.imagenMascota);
            mascotaName = itemView.findViewById(R.id.nombreMascota);
        }

        public ImageView getMascotaImage() {
            return mascotaImage;
        }

        public TextView getMascotaName() {
            return mascotaName;
        }

    }

    public PerfilMascotaAdapter(Context context, List<Buscador> listaPerfilesBuscador) {
        this.listaPerfilesBuscador = listaPerfilesBuscador;
    }

    @NonNull
    @Override
    public PerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PerfilViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_perfil, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PerfilViewHolder holder, int position) {
        Picasso.get().load("http://" + configIp.IP + ":8000" + listaPerfilesBuscador.get(position).getFotoPerfil())
                .placeholder(R.drawable.paw_solid)
                .into(holder.getMascotaImage());

        holder.getMascotaName().setText(listaPerfilesBuscador.get(position).getNombreMascota());

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("idBuscado", listaPerfilesBuscador.get(position).getIdMascota());
            FragmentoPerfilVisitado fragmentoPerfilVisitado = new FragmentoPerfilVisitado();
            fragmentoPerfilVisitado.setArguments(bundle);
            Metodos.openFragment(fragmentoPerfilVisitado, v.getContext());

        });
    }

    @Override
    public int getItemCount() {
        return listaPerfilesBuscador.size();
    }

}
