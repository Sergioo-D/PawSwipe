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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class verificarLogin {

    public void login (String url, Context contexto, Usuario usuario, Response.Listener<String> callback, Response.ErrorListener errorListener){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                        callback.onResponse(response);

                    },
                error -> {
                    errorListener.onErrorResponse(error);

                }
                ){
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
