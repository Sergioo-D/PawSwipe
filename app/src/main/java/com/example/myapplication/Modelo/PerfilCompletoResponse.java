package com.example.myapplication.Modelo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PerfilCompletoResponse implements Serializable {
    @SerializedName("mascota_actual")
    private Mascota mascotaActual;

    @SerializedName("todas_las_mascotas")
    private List<Mascota> todasLasMascotas;

    public Mascota getMascotaActual() {
        return mascotaActual;
    }

    public void setMascotaActual(Mascota mascotaActual) {
        this.mascotaActual = mascotaActual;
    }

    public List<Mascota> getTodasLasMascotas() {
        return todasLasMascotas;
    }

    public void setTodasLasMascotas(List<Mascota> todasLasMascotas) {
        this.todasLasMascotas = todasLasMascotas;
    }
}
