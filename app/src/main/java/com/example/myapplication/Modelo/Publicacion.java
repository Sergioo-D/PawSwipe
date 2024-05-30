package com.example.myapplication.Modelo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Publicacion implements Serializable {

    @SerializedName("nombreMascota")
    private String nombreMascota;

    @SerializedName("fotoPerfil")
    private String fotoPerfil;

    @SerializedName("id")
    private String id;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("fechaPublicacion")
    private String fechaPublicacion;
    @SerializedName("imagenes")
    private List<Imagen> imagenes;
    @SerializedName("comentarios")
    private List<Comentario> comentarios;
    @SerializedName("likes")
    private int likes; // Número total de likes

    private List<String> listaImagenes;
    private String idMascota;
    @SerializedName("likesPerfiles")
    private List<String> perfilesLikes; // Lista de nombres de perfiles que han dado like

    // Constructor actualizado
    public Publicacion(String id, String descripcion, String fechaPublicacion, List<Imagen> imagenes, int likes, List<String> perfilesLikes, List<Comentario> comentarios) {
        this.descripcion = descripcion;
        this.fechaPublicacion = fechaPublicacion;
        this.imagenes = imagenes;
        this.likes = likes;
        this.perfilesLikes = perfilesLikes;
        this.comentarios = comentarios;
        this.id = id;
    }

    public Publicacion(String descripcion, List<String> listaImagenes, String idMascota) {
        this.descripcion = descripcion;
        this.listaImagenes = listaImagenes;
        this.idMascota = idMascota;
    }

    // Getters y Setters
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<Imagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
    }

    public int getLikes() {
        return likes;
    }

    public int setLikes(int likes) {
        this.likes = likes;
        return this.likes;
    }

    public List<String> getPerfilesLikes() {
        return perfilesLikes;
    }

    public void setPerfilesLikes(List<String> perfilesLikes) {
        this.perfilesLikes = perfilesLikes;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getListaImagenes() {
        return listaImagenes;
    }

    public void setListaImagenes(List<String> listaImagenes) {
        this.listaImagenes = listaImagenes;
    }

    public String getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(String idMascota) {
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
}
