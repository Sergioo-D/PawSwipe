package com.example.myapplication.Modelo;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.Temporal;
import java.util.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.TimeZone;

public class Comentario implements Serializable {
    @SerializedName("texto")
    private String texto;
    @SerializedName("fecha_creacion")
    private String fechaCreacion;
    @SerializedName("perfil_info")
    private PerfilInfo perfilInfo;

    @SerializedName("id")
    private String id;


    private String fotoPerfil;

    private String nombreMascota;

    private String idPublicacion;
    private String idMascota;


    public Comentario(String texto, String fechaCreacion, String fotoPerfil, String nombreMascota) {
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        this.fotoPerfil = fotoPerfil;
        this.nombreMascota = nombreMascota;
    }

    public Comentario (String texto,  String idPublicacion, String idMascota) {
        this.texto = texto;
        this.fechaCreacion = fechaCreacion;
        this.idPublicacion=idPublicacion;
        this.idMascota = idMascota;
    }
    

    // Getters y Setters


    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

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

    public String getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(String idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public String getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(String idMascota) {
        this.idMascota = idMascota;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PerfilInfo getPerfilInfo() {
        return perfilInfo;
    }

    public void setPerfilInfo(PerfilInfo perfilInfo) {
        this.perfilInfo = perfilInfo;
    }

    public String getFechaRelativa() {
        String fechaInput = this.fechaCreacion.substring(0, this.fechaCreacion.lastIndexOf('.') + 4) + this.fechaCreacion.substring(this.fechaCreacion.indexOf('+'));
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

        try {
            Date date = isoFormat.parse(fechaInput);
            return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());  // Imprime el mensaje de error
            return "Fecha desconocida";
        }
    }


}

