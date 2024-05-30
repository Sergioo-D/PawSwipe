package com.example.myapplication.vista.Fragmentos;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapters.MascotaAdapter;
import com.example.myapplication.Adapters.PublicacionAdapter;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.InsertSeguir;
import com.example.myapplication.DAL.ShowPerfilVisitado;
import com.example.myapplication.Metodos;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FragmentoPerfilVisitado extends Fragment {
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

    private Boolean estaSiguiendo = false;

    private Button botonSeguir;
    private List<Publicacion> publicaciones = new ArrayList<>();
    private List<Mascota> mascotas = new ArrayList<>(); // Actualizamos a List<Mascota>
    private Mascota mascotaActual; // Variable para almacenar la mascota actual

    private Boolean cambio = false;
    private String idCambio;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_perfil_visitado, container, false);
        petNameView = view.findViewById(R.id.textViewNombreMascota);
        petDescriptionView = view.findViewById(R.id.textViewDescripcionMascota);
        petImageView = view.findViewById(R.id.imageViewPerfilMascota);
        numPublicaciones = view.findViewById(R.id.tvNumPublicaciones);
        numSeguidores = view.findViewById(R.id.tvNumSeguidores);
        numSeguidos = view.findViewById(R.id.tvNumSeguidos);
        botonEditar = view.findViewById(R.id.botonEnviarMensaje);
        botonSeguir = view.findViewById(R.id.botonseguir);
        recyclerViewPublicaciones = view.findViewById(R.id.recyclerViewPublicaciones);
        recyclerViewMascotas = view.findViewById(R.id.recyclerViewMascotas);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUI();

        botonSeguir.setOnClickListener(v -> {
            String url = "http://" + configIp.IP + ":8000/api/follow_perfil/";
            InsertSeguir insertSeguir = new InsertSeguir();
            insertSeguir.seguir(url, getContext(), Metodos.idPerfilVisitado, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    String num_seguidores = jsonObject.getString("num_seguidores");
                    if (message.equals("Seguir")) {
                        botonSeguir.setText("Dejar de seguir");
                    } else {
                        botonSeguir.setText("Seguir");
                    }
                    numSeguidores.setText(num_seguidores);
                    updateUI();
                } catch (JSONException e) {
                    Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                }
            }, error -> {
                Log.e("Error seguir", "Error server: " + error.getMessage());
            });

        });

    }

    public void updateUI() {
        int idMascota;
        if (!cambio) {
            idMascota = requireArguments().getInt("idBuscado");
        } else {
            idMascota = Integer.parseInt(idCambio);
        }


        ShowPerfilVisitado perfilVisitado = new ShowPerfilVisitado();
        String url = "http://" + configIp.IP + ":8000/api/perfil_completo_visitado/";
        perfilVisitado.datosPerfilVisitado(url, getContext(), idMascota,
                response -> {
                    Gson gson = new Gson();
                    try {
                        PerfilCompletoResponse perfilResponse = gson.fromJson(response, PerfilCompletoResponse.class);
                        SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                        Metodos.idPerfilMascota = prefs.getString("perfil_id_actual", "");
                        Metodos.idPerfilVisitado = String.valueOf(perfilResponse.getMascotaActual().getPerfil().getId());
                        petNameView.setText(perfilResponse.getMascotaActual().getNombre());
                        String imageUrl = "http://" + configIp.IP + ":8000" + perfilResponse.getMascotaActual().getPerfil().getFotoPerfil();
                        Picasso.get().load(imageUrl).placeholder(R.drawable.paw_solid).into(petImageView);
                        for (int i = 0; i < perfilResponse.getMascotaActual().getPerfil().getSiguiendo().size() && !estaSiguiendo; i++) {
                            if (Metodos.idPerfilMascota.equals(String.valueOf(perfilResponse.getMascotaActual().getPerfil().getSiguiendo().get(i)))) {
                                estaSiguiendo = true;
                            } else {
                                estaSiguiendo = false;
                            }
                        }

                        if (estaSiguiendo) {
                            botonSeguir.setText("Dejar de seguir");
                        } else {
                            botonSeguir.setText("Seguir");
                        }

                        petDescriptionView.setText(perfilResponse.getMascotaActual().getDescripcion());
                        numPublicaciones.setText(String.valueOf(perfilResponse.getMascotaActual().getPerfil().getTotalPublicaciones()));
                        numSeguidores.setText(String.valueOf(perfilResponse.getMascotaActual().getPerfil().getTotalSeguidores()));
                        numSeguidos.setText(String.valueOf(perfilResponse.getMascotaActual().getPerfil().getTotalSiguiendo()));
                        MascotaAdapter mascotaAdapter = new MascotaAdapter(perfilResponse.getTodasLasMascotas(),
                                new MascotaAdapter.OnMascotaClickListener() {
                                    @Override
                                    public void onMascotaSelected(Mascota mascota) {
                                        idCambio = mascota.getId();
                                        cambio = true;
                                        updateUI();
                                    }

                                    @Override
                                    public void onAddPet() {
                                    }
                                }, false);
                        recyclerViewMascotas.setAdapter(mascotaAdapter);
                        PublicacionAdapter publicacionAdapter = new PublicacionAdapter(getContext(), perfilResponse.getMascotaActual().getPerfil().getPublicaciones(), this);
                        recyclerViewPublicaciones.setAdapter(publicacionAdapter);

                    } catch (Exception e) {
                        Log.e("GsonError", "Error parsing JSON with Gson: " + e.getMessage());
                    }

                }, error -> {

                });
    }

//    public void setupRecyclerViewPublicaciones(List<Publicacion> publicaciones) {
//        PublicacionAdapter adapter = new PublicacionAdapter(getContext(), publicaciones, this);
//        recyclerViewPublicaciones.setAdapter(adapter);
//        recyclerViewPublicaciones.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
//        recyclerViewPublicaciones.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true));
//    }

   /* private void setupRecyclerViewMascotas(List<Mascota> mascotas) {
        MascotaAdapter adapter = new MascotaAdapter(mascotas, new MascotaAdapter.OnMascotaClickListener() {
            @Override
            public void onMascotaSelected(Mascota mascota) {
                ((ActivityHome) getActivity()).saveSelectedMascotaId(mascota.getId());
                ((ActivityHome) getActivity()).loadProfileFragment();
            }

        });

        recyclerViewMascotas.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMascotas.setAdapter(adapter);
    }*/
}