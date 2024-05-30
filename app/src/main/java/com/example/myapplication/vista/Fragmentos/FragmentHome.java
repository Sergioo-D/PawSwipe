package com.example.myapplication.vista.Fragmentos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.Adapters.FeedAdapter;
import com.example.myapplication.Adapters.PublicacionesAdapter;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.ShowFeed;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class FragmentHome extends Fragment {

    private RecyclerView recyclerView;

    private FeedAdapter adapter;

    private List<Publicacion> publicaciones;

    private ConfigIp configIp = new ConfigIp();
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerViewPublicacionesMuro);

        super.onViewCreated(view, savedInstanceState);
        String url = "http://" + configIp.IP +  ":8000/api/muro/";
        ShowFeed showFeed = new ShowFeed();
        showFeed.obtenerFeed(url, getContext(), response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String jsonArrayString = jsonObject.getJSONArray("publicaciones").toString();
                Gson gson = new Gson();
                Type tipoListaPublicaciones = new TypeToken<List<Publicacion>>(){}.getType();
                List<Publicacion> publicaciones = gson.fromJson(jsonArrayString, tipoListaPublicaciones);
                adapter = new FeedAdapter(getContext(), publicaciones);

                recyclerView.setAdapter(adapter);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        }, error -> {

        });

    }
}