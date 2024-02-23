package com.example.myapplication.vista.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.DAL.BloquearCuenta;
import com.example.myapplication.R;
import com.example.myapplication.Modelo.Usuario;
import com.example.myapplication.DAL.verificarLogin;

import org.json.JSONException;
import org.json.JSONObject;


public class ActivityLogin extends AppCompatActivity {

    Integer contadorIntentos = 0;
    Boolean isLogin = Boolean.FALSE;
    Boolean bloqueada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLogin){
            Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
            startActivity(intent);
            finish();
        }else{
            setContentView(R.layout.activity_login);
            getSupportActionBar().hide();
            Button botonInicio = findViewById(R.id.button);
            ImageView textViewLogo = findViewById(R.id.textViewLogo);
            EditText etEmail = findViewById(R.id.editTextTextEmailAddress);
            EditText etPassword = findViewById(R.id.editTextTextPassword);
            Button OlvPass = findViewById(R.id.olvidoPass);
            TextView registro = findViewById(R.id.registro);

            OlvPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityLogin.this, ActivityRecuperarPass.class);
                    startActivity(intent);
                    finish();
                }
            });
            registro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent_registrer = new Intent(ActivityLogin.this, ActivityRegistro.class);
                    startActivity(intent_registrer);
                    finish();
                }

            });

            botonInicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    if (password.isEmpty() ||email.isEmpty()) {
                        Toast.makeText(ActivityLogin.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
                    }else {

                        verificarLogin login = new verificarLogin();
                        Usuario usuario = new Usuario(email, password);
                       // if (!bloqueada) {
                            login.login("http://10.1.105.37:8000/logear/",
                                    ActivityLogin.this, usuario, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.d("RESPONSE_TAG", "Response from server: " + response); // Agregar esta línea para ver la respuesta del servidor en los logs
                                            try {
                                                JSONObject jsonObject = new JSONObject(response);
                                                String message = jsonObject.getString("message");
                                                if (message.equals("1")) {
                                                    Toast.makeText(ActivityLogin.this, "Logeado", Toast.LENGTH_SHORT).show();
                                                    isLogin = Boolean.TRUE;
                                                    Intent intent_registrer = new Intent(ActivityLogin.this, ActivityHome.class);
                                                    intent_registrer.putExtra("EMAIL", email);
                                                    intent_registrer.putExtra("CONTRASEÑA", password);
                                                    startActivity(intent_registrer);
                                                    finish();

                                                } else if (message.equals("0")) {
                                                    contadorIntentos += 1;
                                                    if (contadorIntentos == 3) {
                                                        BloquearCuenta bloquear = new BloquearCuenta();
                                                        bloquear.bloquearCuenta("http://10.1.105.37:8000/bloquear_cuenta/",
                                                                ActivityLogin.this, usuario, new Response.Listener<String>() {
                                                                    @Override
                                                                    public void onResponse(String response) {
                                                                        Log.d("RESPONSE_TAG", "Response from server: " + response);
                                                                        try {
                                                                            JSONObject jsonObject = new JSONObject(response);
                                                                            String message = jsonObject.getString("message");
                                                                            if (message.equals("1")) {
                                                                                Toast.makeText(ActivityLogin.this, "La cuenta ha sido bloqueada", Toast.LENGTH_SHORT).show();
                                                                                contadorIntentos = 0;
                                                                                bloqueada = true;
                                                                            }

                                                                        } catch (JSONException e) {
                                                                            Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        if (!bloqueada) {
                                                            Toast.makeText(ActivityLogin.this,
                                                                    "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                                        }else {Toast.makeText(ActivityLogin.this, "La cuenta está bloqueada por fallar 3 veces", Toast.LENGTH_SHORT).show();}
                                                    }
                                                }

                                            } catch (JSONException e) {
                                                Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                                            }

                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("ERROR TAG", "Error from server: " + error.getMessage());
                                            Toast.makeText(ActivityLogin.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                       // } else {Toast.makeText(ActivityLogin.this, "La cuenta está bloqueada por fallar 3 veces", Toast.LENGTH_SHORT).show();}


                    }}


    });}}}