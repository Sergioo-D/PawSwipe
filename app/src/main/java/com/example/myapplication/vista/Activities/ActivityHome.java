package com.example.myapplication.vista.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.CerrarSesion;
import com.example.myapplication.DAL.DeleteUser;
import com.example.myapplication.DAL.ShowPerfil;
import com.example.myapplication.Interface.DataChangeListener;
import com.example.myapplication.Modelo.Perfil;
import com.example.myapplication.Modelo.PerfilCompletoResponse;
import com.example.myapplication.vista.Fragmentos.FragmentHome;
import com.example.myapplication.vista.Fragmentos.FragmentoBuscador;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfil;
import com.example.myapplication.R;
import com.example.myapplication.Modelo.Usuario;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfilVisitado;
import com.example.myapplication.vista.Fragmentos.FragmentoPublicar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityHome extends AppCompatActivity {

    ConfigIp configIp = new ConfigIp();
    private FragmentoPerfil fragmentoPerfil;
    private FragmentoPerfilVisitado fragmentoPerfilVisitado;
    private FragmentHome fragmentHome;

    private FragmentoPublicar fragmentoPublicar;

    private FragmentoBuscador fragmentoBuscador;
    private ActionBar toolbar;

    private String token;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_perfil:
                    toolbar.setTitle("Perfil");
                    if (fragmentoPerfil == null) {
                        fragmentoPerfil = new FragmentoPerfil();
                    }
                    loadProfileFragment();
                    return true;
                case R.id.navigation_feed:
                    toolbar.setTitle("Home");
                    fragmentHome = new FragmentHome();
                    openFragment(fragmentHome);
                    return true;
                case R.id.publicar:
                    toolbar.setTitle("Publicar");
                    fragmentoPublicar = new FragmentoPublicar();
                    openFragment(fragmentoPublicar);
                    return true;
                case R.id.navigation_search:
                    toolbar.hide();
                    fragmentoBuscador = new FragmentoBuscador();
                    openFragment(fragmentoBuscador);
//                    fragmentoPerfilVisitado = new FragmentoPerfilVisitado();
//                    openFragment(fragmentoPerfilVisitado);


                    // Tu lógica para la pestaña de artistas
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.barra_superior, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_logout:
                CerrarSesion cerrarSesion = new CerrarSesion();
                cerrarSesion.cerrarSesion("http://" + configIp.IP + ":8000/cerrar_sesion/", ActivityHome.this, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Mostrar mensaje de éxito
                        Toast.makeText(ActivityHome.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

                        // Redirigir al inicio de sesión
                        Intent intent = new Intent(ActivityHome.this, ActivityLogin.class);
                        startActivity(intent);
                        finish();
                    }
                }, error -> {
                    Toast.makeText(ActivityHome.this, "Error al cerrar sesión: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

                return true;

            case R.id.borrar_cuenta:
                String email = getIntent().getStringExtra("EMAIL");
                Usuario user = new Usuario(email);
                DeleteUser delete = new DeleteUser();
                delete.borrarUsuario("http://" + configIp.IP + ":8000/borrar_usuario/", ActivityHome.this, user, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("Response", response);
                            JSONObject jsonObject = new JSONObject(response);
                            String message = jsonObject.getString("message");
                            if (message.equals("1")) {
                                Log.d("RESPONSE_TAG", "Response from server: " + response);
                                Toast.makeText(ActivityHome.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ActivityHome.this, ActivityLogin.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d("RESPONSE_TAG", "Response from server: " + response);
                            }
                        } catch (JSONException e) {
                            Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                });
                return true;
//            case R.id.modificar:
//                String correo = getIntent().getStringExtra("EMAIL");
//                Intent intent_update = new Intent(ActivityHome.this, ActivityModificar.class);
//                intent_update.putExtra("EMAIL", correo);
//                startActivity(intent_update);
//                finish();
//                return true;
            case R.id.md:
                Intent intent_md = new Intent(ActivityHome.this, ActivityMD.class);
                startActivity(intent_md);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_prueba);
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        token = prefs.getString("token", "false");
        toolbar = getSupportActionBar();
        toolbar.setTitle("Home");
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    public void loadProfileFragment() {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String lastSelectedMascotaId = prefs.getString("last_selected_mascota_id", null);

        String url = "http://" + configIp.IP + ":8000/api/perfil_completo/";
        if (lastSelectedMascotaId != null) {
            url += "?mascota_id=" + lastSelectedMascotaId;
        }

        ShowPerfil data = new ShowPerfil();
        data.datosPerfil(url, ActivityHome.this, token, response -> {
            if (response != null) {
                Gson gson = new Gson();
                try {
                    PerfilCompletoResponse perfilResponse = gson.fromJson(response, PerfilCompletoResponse.class);
                    if (perfilResponse.getMascotaActual() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("perfilResponse", gson.toJson(perfilResponse));
                        fragmentoPerfil = new FragmentoPerfil();
                        fragmentoPerfil.setArguments(bundle);
                        openFragment(fragmentoPerfil);
                    } else {
                        Log.e("API Error", "No se encontró la mascota actual en la respuesta.");
                    }
                } catch (Exception e) {
                    Log.e("GsonError", "Error parsing JSON with Gson: " + e.getMessage());
                }
            }
        });
    }

    public void saveSelectedMascotaId(String mascotaId, String idPerfil) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_selected_mascota_id", mascotaId);
        editor.putString("perfil_id_actual", idPerfil);
        editor.apply();
    }

   /* public void updateProfileFragmentDirectly(Perfil perfil) {
        FragmentoPerfil fragmentoPerfil = (FragmentoPerfil) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragmentoPerfil != null) {
            fragmentoPerfil.updateProfileDirectly(perfil);
        } else {
            Log.e("ActivityHome", "FragmentoPerfil no encontrado");
        }
    }*/


    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}