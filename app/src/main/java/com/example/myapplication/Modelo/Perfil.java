package com.example.myapplication.Modelo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Perfil implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("mascota")
    private Mascota mascota;
    @SerializedName("fotoPerfil")
    private String fotoPerfil;
    @SerializedName("totalPublicaciones")
    private int totalPublicaciones;
    @SerializedName("siguiendo")
    private List<Integer> siguiendo;
    @SerializedName("seguidores")
    private List<Integer> seguidores;
    @SerializedName("publicaciones")
    private List<Publicacion> publicaciones;

    public Perfil(Mascota mascota, String fotoPerfil) {
        this.mascota = mascota;
        this.fotoPerfil = fotoPerfil;
        this.siguiendo = new ArrayList<>();
        this.seguidores = new ArrayList<>();
        this.totalPublicaciones = 0; // Inicializar total de publicaciones si es necesario
        this.publicaciones = new ArrayList<>();
    }

   /* public void seguirPerfil(Integer idPerfil) {
        if (!siguiendo.contains(idPerfil)) {
            siguiendo.add(idPerfil);
            idPerfil.getSeguidores().add(this);
        }
    }*/

    public int getId() {
        return id;
    }

    public int getTotalSeguidores() {
        return seguidores.size();
    }

    public int getTotalSiguiendo() {
        return siguiendo.size();
    }

    public List<Integer> getSeguidores() {
        return seguidores;
    }

    public List<Integer> getSiguiendo() {
        return siguiendo;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public List<Publicacion> getPublicaciones() {
        return publicaciones;
    }

    public void setPublicaciones(List<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
    }

    public int getTotalPublicaciones() {
        return totalPublicaciones;
    }

    public void setTotalPublicaciones(int totalPublicaciones) {
        this.totalPublicaciones = totalPublicaciones;
    }

    @Override
    public String toString() {
        return "Perfil de " + mascota.getNombre();
    }
}
