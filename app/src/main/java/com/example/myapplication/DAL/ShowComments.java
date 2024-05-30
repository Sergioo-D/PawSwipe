package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ShowComments {

    public void obtenerComentarios(String url, Context contexto, Response.Listener<String> listener) {
        SharedPreferences prefs = contexto.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> listener.onResponse(response),
                error -> {
                    listener.onResponse(error.toString());  // En la práctica, podrías manejar errores de manera más específica
                    Log.e("Error de API", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
