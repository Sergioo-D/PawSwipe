package com.example.myapplication.vista.Fragmentos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Adapters.PerfilMascotaAdapter;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.GetPerfiles;
import com.example.myapplication.Modelo.Buscador;
import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentoBuscador extends Fragment {

    private EditText editTextSearch;
    private RecyclerView recyclerViewResults;
    private PerfilMascotaAdapter adapter;
    private RequestQueue requestQueue;

    private ConfigIp configIp = new ConfigIp();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_buscador, container, false);

        editTextSearch = view.findViewById(R.id.etSearch);
        recyclerViewResults = view.findViewById(R.id.rvSearchResults);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));


        requestQueue = Volley.newRequestQueue(getContext());
        setupSearchEditText();

        return view;
    }

    private void setupSearchEditText() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    performSearch(s.toString());
                } else {
                   // adapter.clearItems(); // Limpia los resultados cuando no hay texto.
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementar.
            }
        });
    }

    private void performSearch(String query) {
        String url = "http://" + configIp.IP + ":8000/api/search_perfiles/?q=" + query;

        GetPerfiles getPerfiles = new GetPerfiles();

        getPerfiles.buscarPerfiles(url, getContext(), response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                List<Buscador> mascotas = parseMascotas(jsonResponse.getJSONArray("resultados"));
                adapter = new PerfilMascotaAdapter(getContext(), mascotas);
                recyclerViewResults.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error parsing JSON", Toast.LENGTH_SHORT).show();
            }

        }, error -> Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

    }

    private List<Buscador> parseMascotas(JSONArray resultados) throws JSONException {
        List<Buscador> mascotas = new ArrayList<>();
        for (int i = 0; i < resultados.length(); i++) {
            JSONObject object = resultados.getJSONObject(i);
            Buscador mascota = new Buscador(
                    object.getInt("id"),
                    object.getString("nombre"),
                    object.getString("foto_url"),
                    object.getString("perfil_url")
            );
            mascotas.add(mascota);
        }
        return mascotas;
    }
}
