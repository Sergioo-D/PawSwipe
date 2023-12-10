package com.example.myapplication.vista.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.myapplication.DAL.DeleteUser;
import com.example.myapplication.DAL.showDataUser;
import com.example.myapplication.vista.Fragmentos.FragmentHome;
import com.example.myapplication.vista.Fragmentos.FragmentoPerfil;
import com.example.myapplication.R;
import com.example.myapplication.Modelo.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityHome extends AppCompatActivity {

    private FragmentoPerfil fragmentoPerfil;
    private FragmentHome fragmentHome;
    private ActionBar toolbar;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_perfil:
                    fragmentoPerfil = new FragmentoPerfil();
                    loadProfileFragment();
                    openFragment(fragmentoPerfil);
                    return true;
                case R.id.navigation_feed:
                    toolbar.setTitle("Home");
                    fragmentHome = new FragmentHome();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragmentHome);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_search:
                    toolbar.setTitle("Buscar");

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
                Intent intent_home = new Intent(ActivityHome.this, ActivityLogin.class);
                startActivity(intent_home);
                finish();
                return true;
            case R.id.borrar_cuenta:
                String email = getIntent().getStringExtra("EMAIL");
                Usuario user = new Usuario(email);
                DeleteUser delete = new DeleteUser();
                delete.borrarUsuario("https://uselessutilities.net/ProyetoDAM/deleteUser.php", ActivityHome.this, user, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("1")){
                            Log.d("RESPONSE_TAG", "Response from server: " + response);
                            Toast.makeText(ActivityHome.this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ActivityHome.this, ActivityLogin.class);
                            startActivity(intent);
                            finish();
                        }else{Log.d("RESPONSE_TAG", "Response from server: " + response);}
                    }
                });
                return true;
            case R.id.modificar:
                String correo = getIntent().getStringExtra("EMAIL");
                Intent intent_update = new Intent(ActivityHome.this, ActivityModificar.class);
                intent_update.putExtra("EMAIL", correo);
                startActivity(intent_update);
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
        toolbar = getSupportActionBar();
        toolbar.setTitle("Home");
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void loadProfileFragment() {
        String email = getIntent().getStringExtra("EMAIL");
        if (email != null) {
            Usuario user = new Usuario(email);
            showDataUser data = new showDataUser();
            data.datosUser("https://uselessutilities.net/ProyetoDAM/getDataUser.php",
                    ActivityHome.this, user, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("Response", response);
                                JSONObject jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                if (message.equals("1")) {
                                    String userName = jsonObject.getString("username");
                                    String fullName = jsonObject.getString("fullname");
                                    String userEmail = jsonObject.getString("email");
                                    openFragment(fragmentoPerfil);
                                    displayProfileFragment(userName, fullName, userEmail);
                                }
                            } catch (JSONException e) {
                                Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    });
        } else {
            Log.e("ActivityDatosUser", "et_email o et_password es nulo");
        }
    }

    private void displayProfileFragment(String userName, String fullName, String userEmail) {
        toolbar.setTitle("Perfil");

        // Cargar el fragmento de perfil si aún no está cargado
        if (fragmentoPerfil == null) {
            fragmentoPerfil = new FragmentoPerfil();
            openFragment(fragmentoPerfil);
        }

        // Actualizar la información del perfil
        if (fragmentoPerfil != null) {
            fragmentoPerfil.updateProfileInfo(userName, fullName, userEmail);
        }
    }

    private void openFragment(FragmentoPerfil fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();}
    }
}