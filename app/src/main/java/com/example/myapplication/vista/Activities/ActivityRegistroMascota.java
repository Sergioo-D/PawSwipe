package com.example.myapplication.vista.Activities;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.InsertMascota;
import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.Modelo.Perfil;
import com.example.myapplication.R;
import com.example.myapplication.Utils.MascotaViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityRegistroMascota extends AppCompatActivity {
    ConfigIp configIp = new ConfigIp();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_mascota);

        EditText etNombreMascota = findViewById(R.id.editTextNombreMascota);
        EditText etDescripcionMascota = findViewById(R.id.editTextDescripcionMascota);
        Button registrarse = findViewById(R.id.registarse);

        final String origen = getIntent().getStringExtra("origen");

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreMascota = etNombreMascota.getText().toString();
                String descripcionMascota = etDescripcionMascota.getText().toString();
                if (nombreMascota.isEmpty()) {
                    Toast.makeText(ActivityRegistroMascota.this, "Por favor, debe rellanar el campo nombre de perfil mascota", Toast.LENGTH_SHORT).show();
                } else {
                    String emailUsuario = getIntent().getStringExtra("EMAIL");
                    Mascota mascota = new Mascota(emailUsuario, nombreMascota, descripcionMascota);
                    InsertMascota insertMascota = new InsertMascota();
                    String url = "http://" + configIp.IP + ":8000/api/registrar_mascota/";
                    insertMascota.registrarMascota(url, ActivityRegistroMascota.this, mascota, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                String idMascota = jsonObject.getString("idMascota");
                                if (message.equals("1")) {
                                    Toast.makeText(ActivityRegistroMascota.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    if ("perfil".equals(origen)) {
                                        saveSelectedMascotaId(idMascota);
                                        setResult(RESULT_OK);
                                        finish();
                                    } else {
                                        Intent intent_registrer = new Intent(ActivityRegistroMascota.this, ActivityLogin.class);
                                        startActivity(intent_registrer);
                                        finish();
                                    }
                                } else if (message.equals("0")) {
                                    Toast.makeText(ActivityRegistroMascota.this, "El nombre de perfil ya existe", Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ActivityRegistroMascota.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void saveSelectedMascotaId(String mascotaId) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("primera_mascota", mascotaId);
        editor.apply();
    }

}
