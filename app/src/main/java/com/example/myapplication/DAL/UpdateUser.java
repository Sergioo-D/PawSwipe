package com.example.myapplication.DAL;

import android.content.Context;

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

public class UpdateUser {
    public void modificarUser(String url, Context contexto, Usuario user, Response.Listener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    listener.onResponse(error.getMessage());

                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> datosUsuario = new HashMap<>();
                datosUsuario.put("email", user.getEmail());
                datosUsuario.put("username", user.getUserName());
                datosUsuario.put("fullname", user.getFullName());

                return datosUsuario;
            }


        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}


