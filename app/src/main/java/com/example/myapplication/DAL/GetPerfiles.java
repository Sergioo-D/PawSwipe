package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Modelo.Mascota;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GetPerfiles {

    public void buscarPerfiles(String url, Context contexto, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        SharedPreferences prefs = contexto.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {

                    listener.onResponse(response);

                },
                error -> listener.onResponse(error.getMessage())
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + token);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
