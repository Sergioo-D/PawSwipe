package com.example.myapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ConfigIp;
import com.example.myapplication.Modelo.Imagen;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private List<Imagen> imagenes;

    ConfigIp configIp = new ConfigIp();

    public ImageSliderAdapter(List<Imagen> imagenes) {
        this.imagenes = imagenes;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Imagen imagen = imagenes.get(position);
        Picasso.get()
                .load("http://" + configIp.IP + ":8000" + imagen.getUrlImagen())
                .fit()
                .centerInside()
                .into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
