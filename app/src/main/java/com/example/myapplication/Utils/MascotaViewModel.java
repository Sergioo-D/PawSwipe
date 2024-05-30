package com.example.myapplication.Utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.Modelo.Perfil;

import java.util.ArrayList;
import java.util.List;

public class MascotaViewModel extends ViewModel {
    private final MutableLiveData<List<Perfil>> mascotas = new MutableLiveData<>();

    public LiveData<List<Perfil>> getMascotas() {
        return mascotas;
    }

    public void agregarMascota(Perfil nuevaMascota) {
        List<Perfil> actualMascotas = mascotas.getValue();
        if (actualMascotas == null) {
            actualMascotas = new ArrayList<>();
        }
        actualMascotas.add(nuevaMascota);
        mascotas.setValue(actualMascotas);
    }
}
