package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CerrarSesion {
    private static final String PREFERENCES_NAME = "MisPreferencias";

    public void cerrarSesion(String url, Context contexto, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        SharedPreferences prefs = contexto.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            // Limpia SharedPreferences después de un cierre exitoso
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("token");
            editor.putBoolean("isLogin", false);
            editor.apply();

            listener.onResponse(response);
        }, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + token);
                // Elimina cualquier ajuste incorrecto de Content-Type si no es necesario
                headers.put("Content-Type", "application/json"); // Si tu API no necesita un cuerpo, esta línea podría incluso omitirse
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
