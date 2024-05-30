package com.example.myapplication.vista.Activities;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.myapplication.Adapters.PublicacionesAdapter;
import com.example.myapplication.Interface.DataChangeListener;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;

import java.util.List;

public class ActivityPublicacion extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PublicacionesAdapter adapter;
    private Boolean esPerfil;
    private List<Publicacion> publicaciones;
    private int currentPosition;


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicacion);
        getSupportActionBar().hide();
        recyclerView = findViewById(R.id.recyclerViewPublicacionesDetalle);

        // Recibir datos
        if (getIntent().hasExtra("publicaciones")) {
            publicaciones = (List<Publicacion>) getIntent().getSerializableExtra("publicaciones");
            currentPosition = getIntent().getIntExtra("position", 0);
            esPerfil = getIntent().getBooleanExtra("esPerfil", true);
        }

        // Configurar RecyclerView
        adapter = new PublicacionesAdapter(this, publicaciones, esPerfil);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Desplazar a la publicación seleccionada
        recyclerView.scrollToPosition(currentPosition);
    }
}
