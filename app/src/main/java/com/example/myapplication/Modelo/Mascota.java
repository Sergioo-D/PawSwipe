package com.example.myapplication.Modelo;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Mascota implements Serializable {
    @SerializedName("id")
    private String id;
    private String emailUsuario;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("perfil")
    private Perfil perfil;  // Referencia al Perfil asociado

    private Bitmap fotoPerfil;

    public Mascota(String nombre, String descripcion, Perfil perfil) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.perfil = perfil;  // Inicializar el perfil en el constructor
    }

    public Mascota(String emailUsuario, String nombre, String descripcion) {
        this.emailUsuario = emailUsuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Mascota(String nombre, String descripcion, Bitmap fotoPerfil) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fotoPerfil = fotoPerfil;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
