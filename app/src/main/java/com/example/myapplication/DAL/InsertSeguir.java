package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class InsertSeguir {

    public void seguir(String url, Context contexto, String idPerfil, Response.Listener<String> callback, Response.ErrorListener errorListener) {
        SharedPreferences prefs = contexto.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("token", ""); // Recupera el token de SharedPreferences
        String idMascota = prefs.getString("last_selected_mascota_id", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            callback.onResponse(response);
        }, error -> {
            // Podrías agregar manejo específico si el error es de autenticación
            errorListener.onErrorResponse(error);
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("perfilId", idPerfil);
                    jsonBody.put("idMascota", idMascota);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + token); // Añade el token al header
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
