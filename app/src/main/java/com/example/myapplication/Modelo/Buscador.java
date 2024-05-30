package com.example.myapplication.Modelo;

public class Buscador {

    private int idMascota;
    private String nombreMascota;
    private String fotoPerfil;

    private String urlPerfil;

    public Buscador(int idMascota, String nombreMascota, String fotoPerfil, String urlPerfil) {
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.fotoPerfil = fotoPerfil;
        this.urlPerfil = urlPerfil;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getUrlPerfil() {
        return urlPerfil;
    }

    public void setUrlPerfil(String urlPerfil) {
        this.urlPerfil = urlPerfil;
    }
}
