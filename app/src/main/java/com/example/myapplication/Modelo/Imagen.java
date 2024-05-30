package com.example.myapplication.Modelo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Imagen implements Serializable {
    @SerializedName("urlImagen")
    private String urlImagen;
    @SerializedName("descripcionImagen")
    private String descripcionImagen;

    public Imagen(String urlImagen, String descripcionImagen) {
        this.urlImagen = urlImagen;
        this.descripcionImagen = descripcionImagen;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getDescripcionImagen() {
        return descripcionImagen;
    }

    public void setDescripcionImagen(String descripcionImagen) {
        this.descripcionImagen = descripcionImagen;
    }
}

