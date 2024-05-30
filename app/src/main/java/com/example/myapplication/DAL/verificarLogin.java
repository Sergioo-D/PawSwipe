package com.example.myapplication.DAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Modelo.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class verificarLogin {

    public void login(String url, Context contexto, Usuario usuario, Response.Listener<String> callback, Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String token = jsonObject.optString("token");
                        boolean hasMascotas = jsonObject.optBoolean("has_mascotas", false); // optBoolean con default false si no está presente
                        if (!token.isEmpty()) {
                            SharedPreferences prefs = contexto.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("token", token);
                            editor.putBoolean("isLogin", true);
                            editor.putBoolean("hasMascotas", hasMascotas);
                            editor.apply();

                            callback.onResponse(response);
                        } else {
                            String error = jsonObject.optString("error", "Error desconocido");
                            Toast.makeText(contexto, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errorListener.onErrorResponse(new VolleyError("Error parsing JSON"));
                    }
                },
                error -> {
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("mail", usuario.getEmail());
                    jsonBody.put("password", usuario.getPassword());
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(contexto);
        requestQueue.add(stringRequest);
    }
}
