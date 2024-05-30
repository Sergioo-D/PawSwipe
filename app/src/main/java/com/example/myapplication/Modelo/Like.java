package com.example.myapplication.Modelo;

import java.util.Date;

public class Like {
    private Perfil perfil;
    private Publicacion publicacion;
    private Date timestamp;

    public Like(Perfil perfil, Publicacion publicacion) {
        this.perfil = perfil;
        this.publicacion = publicacion;
        this.timestamp = new Date();  // Fecha actual para representar auto_now_add
    }

    // Getters y Setters
    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(Publicacion publicacion) {
        this.publicacion = publicacion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return perfil.getMascota().getNombre() + " ha dado like a la publicación " + publicacion;
    }
}

