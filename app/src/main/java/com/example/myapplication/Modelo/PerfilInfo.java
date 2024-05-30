package com.example.myapplication.Modelo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PerfilInfo implements Serializable {
    @SerializedName("fotoPerfil")
    private String fotoPerfil;

    @SerializedName("nombreMascota")
    private String nombreMascota;

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }
}

