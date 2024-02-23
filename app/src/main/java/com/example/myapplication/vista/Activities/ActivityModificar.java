package com.example.myapplication.vista.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.myapplication.DAL.UpdateUser;
import com.example.myapplication.DAL.showDataUser;
import com.example.myapplication.R;
import com.example.myapplication.Modelo.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityModificar extends AppCompatActivity {
    private ActionBar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar);
        toolbar = getSupportActionBar();
        toolbar.setTitle("Editar perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button update = findViewById(R.id.bUpdate);
        EditText newUsername = findViewById(R.id.etNusername);
        EditText newFullname = findViewById(R.id.etNfullname);
        ImageView fotoPerfil = findViewById(R.id.foto_perfil);

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivityModificar.this, "Aquí abrirá la galeria", Toast.LENGTH_LONG).show();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = newUsername.getText().toString();
                String fullname = newFullname.getText().toString();
                if (username.isEmpty() && fullname.isEmpty()) {
                    Toast.makeText(ActivityModificar.this, "No se ha introducido ningún dato", Toast.LENGTH_SHORT).show();
                } else {
                    String email = getIntent().getStringExtra("EMAIL");
                    Usuario usuario = new Usuario(email, username, fullname);
                    UpdateUser update = new UpdateUser();
                    update.modificarUser("http://10.1.105.37:8000/modificar_usuario/", ActivityModificar.this, usuario, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("Response", response);
                                JSONObject jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                if (message.equals("1")) {
                                    Log.d("RESPONSE_TAG", "Response from server: " + response);
                                    Toast.makeText(ActivityModificar.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                    newUsername.setText("");
                                    newFullname.setText("");
                                    showDataUser mostrar = new showDataUser();
                                } else {
                                    Log.d("RESPONSE_TAG", "Response from server: " + response);
                                    Toast.makeText(ActivityModificar.this, "No se han podido guardar los cambios", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String email = getIntent().getStringExtra("EMAIL");
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, ActivityHome.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}