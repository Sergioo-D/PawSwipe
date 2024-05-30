package com.example.myapplication.DAL;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Modelo.Usuario;

import java.util.HashMap;
import java.util.Map;

public class showDataUser {
    public void datosUser(String url, Context contexto, Usuario user, Response.Listener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    listener.onResponse(error.getMessage());
                    Log.d("Hola", error.getMessage());

                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> datosUsuario = new HashMap<>();
                if (user.getEmail() != null) {
                    datosUsuario.put("email", user.getEmail());
                }

                if (user.getPassword() != null) {
                    datosUsuario.put("password", user.getPassword());
                }

                return datosUsuario;
            }


        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}


