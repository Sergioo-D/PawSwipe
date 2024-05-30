package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Modelo.Publicacion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class InsertPublicacion {

    public void insertPublicacion(String url, Context contexto, Publicacion publicacion, Response.Listener<String> callback) {
        SharedPreferences prefs = contexto.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("token", ""); // Recupera el token de SharedPreferences

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            callback.onResponse(response);
        }, error -> {
            // Podrías agregar manejo específico si el error es de autenticación
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("descripcion", publicacion.getDescripcion());
                    jsonBody.put("idMascota", publicacion.getIdMascota());
                    JSONArray imagenesArray = new JSONArray();
                    for (String imagen : publicacion.getListaImagenes()) {
                        imagenesArray.put(imagen);
                    }
                    jsonBody.put("imagenes", imagenesArray);
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
