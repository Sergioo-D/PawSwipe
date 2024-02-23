package com.example.myapplication.vista.Fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.vista.Activities.ActivityHome;
import com.example.myapplication.vista.Activities.ActivityModificar;

public class FragmentoPerfil extends Fragment {
    private TextView usuario;
    private TextView name;
    private TextView email;
    private Button editar;

    private TextView usuario1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        usuario = view.findViewById(R.id.textViewUser);
        name = view.findViewById(R.id.textViewName);
        email = view.findViewById(R.id.textViewEmail);
        editar = view.findViewById(R.id.boton_editar);
        usuario1 = view.findViewById(R.id.textViewUser1);

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el email del intent
                String email = getActivity().getIntent().getStringExtra("EMAIL");

                // Crear un Intent para iniciar ActivityModificar
                Intent intent = new Intent(getActivity(), ActivityModificar.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }
        });

        return view;
    }



    public void updateProfileInfo(String userName, String fullName, String userEmail) {
        if (usuario != null && name != null && email != null) {
            usuario.setText(userName);
            email.setText(userEmail);
            name.setText(fullName);
            usuario1.setText(userName);
        } else {
            Log.e("FragmentoPerfil", "Alguno de los TextViews no se ha inicializado correctamente");
        }
    }
}
