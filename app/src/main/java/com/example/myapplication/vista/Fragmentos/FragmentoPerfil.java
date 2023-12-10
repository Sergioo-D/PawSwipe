package com.example.myapplication.vista.Fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class FragmentoPerfil extends Fragment {
    private TextView usuario;
    private TextView name;
    private TextView email;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        usuario = view.findViewById(R.id.textViewUser);
        name = view.findViewById(R.id.textViewName);
        email = view.findViewById(R.id.textViewEmail);

        return view;
    }

    public void updateProfileInfo(String userName, String fullName, String userEmail) {
        if (usuario != null && name != null && email != null) {
            usuario.setText(userName);
            email.setText(userEmail);
            name.setText(fullName);
        } else {
            Log.e("FragmentoPerfil", "Alguno de los TextViews no se ha inicializado correctamente");
        }
    }
}
