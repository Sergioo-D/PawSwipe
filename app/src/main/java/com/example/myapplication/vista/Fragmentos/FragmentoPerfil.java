package com.example.myapplication.vista.Fragmentos;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapters.MascotaAdapter;
import com.example.myapplication.Adapters.PublicacionAdapter;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.Modelo.PerfilCompletoResponse;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;
import com.example.myapplication.Utils.GridSpacingItemDecoration;
import com.example.myapplication.vista.Activities.ActivityHome;
import com.example.myapplication.vista.Activities.ActivityModificar;
import com.example.myapplication.vista.Activities.ActivityRegistroMascota;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class FragmentoPerfil extends Fragment  {

    public static final int REQUEST_CODE_PUBLICACION = 1;
    private static final int REQUEST_CODE_EDITAR = 1;

    private static final int REQUEST_CODE_ADD_MASCOTA = 1;

    ConfigIp configIp = new ConfigIp();
    private TextView petNameView;
    private TextView petDescriptionView;
    private ImageView petImageView;
    private TextView numPublicaciones;
    private TextView numSeguidores;
    private TextView numSeguidos;
    private RecyclerView recyclerViewPublicaciones;
    private RecyclerView recyclerViewMascotas;
    private Button botonEditar;
    private List<Publicacion> publicaciones = new ArrayList<>();
    private List<Mascota> mascotas = new ArrayList<>(); // Actualizamos a List<Mascota>
    private Mascota mascotaActual; // Variable para almacenar la mascota actual


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        petNameView = view.findViewById(R.id.textViewNombreMascota);
        petDescriptionView = view.findViewById(R.id.textViewDescripcionMascota);
        petImageView = view.findViewById(R.id.imageViewPerfilMascota);
        numPublicaciones = view.findViewById(R.id.tvNumPublicaciones);
        numSeguidores = view.findViewById(R.id.tvNumSeguidores);
        numSeguidos = view.findViewById(R.id.tvNumSeguidos);
        botonEditar = view.findViewById(R.id.botonEditarPerfil);
        recyclerViewPublicaciones = view.findViewById(R.id.recyclerViewPublicaciones);
        recyclerViewMascotas = view.findViewById(R.id.recyclerViewMascotas);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("perfilResponse")) {
            String json = getArguments().getString("perfilResponse");
            Gson gson = new Gson();
            PerfilCompletoResponse perfilResponse = gson.fromJson(json, PerfilCompletoResponse.class);
            updateUI(perfilResponse);
        }

        botonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityModificar.class);
                intent.putExtra("mascota", mascotaActual);
                startActivityForResult(intent, REQUEST_CODE_EDITAR);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDITAR && resultCode == RESULT_OK && data != null) {
            Mascota mascotaActualizada = (Mascota) data.getSerializableExtra("mascotaActualizada");
            PerfilCompletoResponse nuevaMascotaActual = (PerfilCompletoResponse) data.getSerializableExtra("nueva_mascota_actual");
            if (mascotaActualizada != null) {
                // Actualizar la UI con los nuevos datos
                petNameView.setText(mascotaActualizada.getNombre());
                petDescriptionView.setText(mascotaActualizada.getDescripcion());
                String imageUrl = "http://" + configIp.IP + ":8000" + mascotaActualizada.getPerfil().getFotoPerfil();
                Picasso.get().load(imageUrl).placeholder(R.drawable.paw_solid).into(petImageView);
                // Actualizar la mascota actual
                mascotaActual = mascotaActualizada;
                ((ActivityHome) getActivity()).saveSelectedMascotaId(mascotaActual.getId(), String.valueOf(mascotaActual.getPerfil().getId()));

            }
            if (nuevaMascotaActual != null) {
                Mascota m = nuevaMascotaActual.getMascotaActual();
                ((ActivityHome) getActivity()).saveSelectedMascotaId(m.getId(), String.valueOf(mascotaActual.getPerfil().getId()));
                updateUI(nuevaMascotaActual);

            }
        }
        if (requestCode == REQUEST_CODE_PUBLICACION && resultCode == RESULT_OK) {
            ((ActivityHome) getActivity()).loadProfileFragment();
        }

        if(requestCode == REQUEST_CODE_ADD_MASCOTA && resultCode == RESULT_OK) {
            ((ActivityHome) getActivity()).loadProfileFragment();
        }
    }

    public void updateUI(PerfilCompletoResponse perfilResponse) {
        mascotaActual = perfilResponse.getMascotaActual();
        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("perfil_id_actual", String.valueOf(mascotaActual.getPerfil().getId()));
        editor.apply();
        petNameView.setText(mascotaActual.getNombre());
        petDescriptionView.setText(mascotaActual.getDescripcion());
        String imageUrl = "http://" + configIp.IP + ":8000" + mascotaActual.getPerfil().getFotoPerfil();
        Picasso.get().load(imageUrl).placeholder(R.drawable.paw_solid).into(petImageView);
        numPublicaciones.setText(String.valueOf(mascotaActual.getPerfil().getTotalPublicaciones()));
        numSeguidores.setText(String.valueOf(mascotaActual.getPerfil().getSeguidores().size()));
        numSeguidos.setText(String.valueOf(mascotaActual.getPerfil().getSiguiendo().size()));

        System.out.println("holaaa, estoy aquíííííííííííííííí");

        mascotas = perfilResponse.getTodasLasMascotas();
        setupRecyclerViewMascotas(mascotas);
        setupRecyclerViewPublicaciones(mascotaActual.getPerfil().getPublicaciones());
    }

    public void setupRecyclerViewPublicaciones(List<Publicacion> publicaciones) {
        PublicacionAdapter adapter = new PublicacionAdapter(getContext(), publicaciones, this);
        recyclerViewPublicaciones.setAdapter(adapter);
        recyclerViewPublicaciones.setLayoutManager(new GridLayoutManager(getContext(), 3));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerViewPublicaciones.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));
    }

    private void setupRecyclerViewMascotas(List<Mascota> mascotas) {
        MascotaAdapter adapter = new MascotaAdapter(mascotas, new MascotaAdapter.OnMascotaClickListener() {
            @Override
            public void onMascotaSelected(Mascota mascota) {
                ((ActivityHome) getActivity()).saveSelectedMascotaId(mascota.getId(), String.valueOf(mascota.getPerfil().getId()));
                ((ActivityHome) getActivity()).loadProfileFragment();
            }

            @Override
            public void onAddPet() {
                // Si tu adaptador tiene un botón o acción para agregar mascotas
                Intent intent = new Intent(getActivity(), ActivityRegistroMascota.class);
                intent.putExtra("origen", "perfil");
                startActivityForResult(intent, REQUEST_CODE_ADD_MASCOTA);
            }
        }, true);

        recyclerViewMascotas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMascotas.setAdapter(adapter);
    }
}
