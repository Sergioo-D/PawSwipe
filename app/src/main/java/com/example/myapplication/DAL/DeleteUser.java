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

public class DeleteUser {
    public void borrarUsuario(String url, Context contexto, Usuario user, Response.Listener<String> callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    callback.onResponse(response);
                },
                error -> {
                    callback.onResponse(error.getMessage());
                    Log.d("Hola", error.getMessage().toString());

                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> datosUsuario = new HashMap<>();
                datosUsuario.put("email", user.getEmail());

                return datosUsuario;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
