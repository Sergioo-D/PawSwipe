package com.example.myapplication.vista.Fragmentos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.Adapters.ComentariosAdapter;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.InsertComentario;
import com.example.myapplication.DAL.ShowComments;
import com.example.myapplication.Modelo.Comentario;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ComentariosBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private EditText editTextComentario;
    private Button buttonEnviarComentario;

    private ConfigIp configIp = new ConfigIp();

    private List<Comentario> comentarios;
    private Publicacion publicacion;
    private ComentariosAdapter comentariosAdapter;

    public ComentariosBottomSheetDialogFragment(List<Comentario> comentarios, Publicacion publicacion) {
        this.comentarios = comentarios;
        this.publicacion = publicacion;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comentarios_bottom_sheet, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewComentarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        comentariosAdapter = new ComentariosAdapter(requireContext(), comentarios);
        recyclerView.setAdapter(comentariosAdapter);

        editTextComentario = view.findViewById(R.id.editTextComentario);
        buttonEnviarComentario = view.findViewById(R.id.buttonEnviarComentario);

        buttonEnviarComentario.setOnClickListener(v -> {
            SharedPreferences prefs = getContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            String idMascota = prefs.getString("last_selected_mascota_id", "");
            String comentarioText = editTextComentario.getText().toString();
            if (!comentarioText.isEmpty()) {
                Comentario nuevoComentario = new Comentario(comentarioText, publicacion.getId(), idMascota);
                InsertComentario insertComentario = new InsertComentario();
                String url = "http://" + configIp.IP + ":8000/api/insert_comentario/";
                insertComentario.insertComentario(url, getContext(), nuevoComentario, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        editTextComentario.setText("");
                        actualizarComentarios();
                    }
                }, error -> Log.e("Error comentario", error.getMessage()));
            }
        });

        return view;
    }

    private void actualizarComentarios() {
        ShowComments showComments = new ShowComments();
        String url = "http://" + configIp.IP + ":8000/api/comentarios/" + publicacion.getId() + "/";
        showComments.obtenerComentarios(url, getContext(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("comentarios");
                    Type listType = new TypeToken<List<Comentario>>() {}.getType();
                    List<Comentario> nuevosComentarios = gson.fromJson(jsonArray.toString(), listType);
                    comentariosAdapter.setComentarios(nuevosComentarios);
                    comentariosAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("JSONError", "Error parsing JSON", e);
                }
            }
        });
    }

    public static void show(FragmentActivity activity, List<Comentario> comentarios, Publicacion publicacion) {
        ComentariosBottomSheetDialogFragment bottomSheet = new ComentariosBottomSheetDialogFragment(comentarios, publicacion);
        bottomSheet.show(activity.getSupportFragmentManager(), bottomSheet.getTag());
    }
}
