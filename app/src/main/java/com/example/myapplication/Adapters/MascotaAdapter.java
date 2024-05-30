package com.example.myapplication.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ConfigIp;
import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.Modelo.Perfil;
import com.example.myapplication.R;
import com.example.myapplication.vista.Activities.ActivityHome;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Mascota> mascotas;
    private OnMascotaClickListener listener;
    private Boolean iconoAdd;

    ConfigIp configIp = new ConfigIp();

    private static final int TYPE_PET = 0;
    private static final int TYPE_ADD_PET = 1;

    public MascotaAdapter(List<Mascota> mascotas, OnMascotaClickListener listener, Boolean iconoAdd) {
        this.mascotas = mascotas;
        this.listener = listener;
        this.iconoAdd = iconoAdd;
    }

    public void setMascotas(List<Mascota> nuevasMascotas) {
        this.mascotas = nuevasMascotas;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mascotas.size()) {  // El último ítem será el botón de añadir
            return TYPE_ADD_PET;
        } else {
            return TYPE_PET;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (iconoAdd) {
            if (viewType == TYPE_ADD_PET) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_mascota, parent, false);
                return new AddPetViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
                return new PetViewHolder(view);
            }
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
            return new PetViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PET) {
            PetViewHolder petHolder = (PetViewHolder) holder;
            Mascota mascota = mascotas.get(position);
            petHolder.petName.setText(mascota.getNombre());
            Log.d("SetupRecyclerView", "Mascota: " + mascota.getNombre() + ", URL: " + mascota.getPerfil().getFotoPerfil());
            Picasso.get().load("http://" + configIp.IP + ":8000" + mascota.getPerfil().getFotoPerfil())
                    .placeholder(R.drawable.paw_solid)
                    .into(petHolder.petImage);

            petHolder.itemView.setOnClickListener(v -> {
                Log.d("MascotaAdapter", "Clic en item de mascota");
                if (listener != null) {
                    listener.onMascotaSelected(mascota);
                    Log.d("MascotaAdapter", "Listener no es nulo");
                    String mascotaId = mascota.getId(); // Asegúrate de que getId() está definido y devuelve la ID correcta
                    Log.d("MascotaAdapter", "Mascota id: ");
                    ((ActivityHome) v.getContext()).saveSelectedMascotaId(mascota.getId(), String.valueOf(mascota.getPerfil().getId()));  // Guardar ID
                }
            });
        } else if (iconoAdd) {
            if (getItemViewType(position) == TYPE_ADD_PET) {
                AddPetViewHolder addPetHolder = (AddPetViewHolder) holder;
                addPetHolder.addButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddPet();
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return mascotas.size() + 1;  // Añade 1 por el botón de añadir mascota
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView petImage;
        TextView petName;
        TextView petDescription;

        public PetViewHolder(View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.imagenMascota);
            petName = itemView.findViewById(R.id.nombreMascota);
        }
    }

    public static class AddPetViewHolder extends RecyclerView.ViewHolder {
        Button addButton;

        public AddPetViewHolder(View itemView) {
            super(itemView);
            addButton = itemView.findViewById(R.id.add_pet_button);
        }
    }

    public interface OnMascotaClickListener {
        void onMascotaSelected(Mascota mascota);

        void onAddPet();
    }
}
