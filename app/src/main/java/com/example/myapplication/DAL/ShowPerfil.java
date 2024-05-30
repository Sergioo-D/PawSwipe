package com.example.myapplication.DAL;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Modelo.Usuario;

import java.util.HashMap;
import java.util.Map;

public class ShowPerfil {
    public void datosPerfil(String url, Context contexto, String token, Response.Listener<String> listener) {
        int timeout = 30000;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> listener.onResponse(response),
                error -> {
                    listener.onResponse(error.toString());
                    Log.d("Error de API", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + token);
                // Elimina cualquier ajuste incorrecto de Content-Type si no es necesario
                headers.put("Content-Type", "application/json"); // Si tu API no necesita un cuerpo, esta línea podría incluso omitirse
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}


