package com.example.myapplication.vista.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.myapplication.DAL.InsertUser;
import com.example.myapplication.R;
import com.example.myapplication.Modelo.Usuario;
import com.example.myapplication.metodos;


public class ActivityRegistro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        getSupportActionBar().hide();

        ImageButton botonAtras = findViewById(R.id.botonAtras);
        EditText etEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText etFullname = findViewById(R.id.editTextNombreCompleto);
        EditText etUsername = findViewById(R.id.editTextTextNombreUsuario);
        EditText etPassword = findViewById(R.id.editTextTextPassword);
        Button boton = findViewById(R.id.button);

        botonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityRegistro.this, ActivityLogin.class);
                startActivity(intent);
                finish();
            }
        });

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String fullname = etFullname.getText().toString();
                String email = etEmail.getText().toString();

                if (username.isEmpty() || password.isEmpty() || fullname.isEmpty() || email.isEmpty()) {
                    Toast.makeText(ActivityRegistro.this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();

                }else{
                    if (!metodos.passwordValidate(password)){
                        etPassword.setError("La contraseña debe contener almenos una mayuscula, número y caracter especial");
                    }else{

                    InsertUser registrarUsuario = new InsertUser();
                    Usuario user = new Usuario(username, metodos.hashPassword(password) , fullname, email);

                    registrarUsuario.insertarUsuario("https://uselessutilities.net/ProyetoDAM/insertarUsuario.php",
                            ActivityRegistro.this, user, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (response.equalsIgnoreCase("1")){
                                        Toast.makeText(ActivityRegistro.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                                        Intent intent_registrer = new Intent(ActivityRegistro.this, ActivityLogin.class);
                                        startActivity(intent_registrer);
                                        finish();
                                    }else {
                                        Toast.makeText(ActivityRegistro.this,
                                                "No se ha podido registrar", Toast.LENGTH_SHORT).show();
                                    }

                                }


                            });
            }}}
        });


    }



}