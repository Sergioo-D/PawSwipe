package com.example.myapplication.vista.Fragmentos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.InsertPublicacion;
import com.example.myapplication.Modelo.Publicacion;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentoPublicar extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private LinearLayout imageContainer;

    ConfigIp configIp = new ConfigIp();
    private ArrayList<Bitmap> selectedBitmaps = new ArrayList<>();
    private ArrayList<String> encodedImages = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        ArrayList<Uri> imageUris = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                        displaySelectedImages(imageUris);
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        ArrayList<Uri> imageUris = new ArrayList<>();
                        imageUris.add(imageUri);
                        displaySelectedImages(imageUris);
                    }
                }
            }
    );

    public FragmentoPublicar() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_publicar, container, false);

        imageContainer = view.findViewById(R.id.imageContainer);
        TextView selectImageTextView = view.findViewById(R.id.selectImageTextView);
        Button botonPublicar = view.findViewById(R.id.buttonPublish);
        EditText textoDescripcion = view.findViewById(R.id.editTextDescription);

        botonPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encodeImages();
                String descripcion = textoDescripcion.getText().toString();
                SharedPreferences prefs = requireContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                String idMascota = prefs.getString("last_selected_mascota_id", "");

                Log.d("FragmentoPublicar", "Descripción: " + descripcion);
                Log.d("FragmentoPublicar", "ID Mascota: " + idMascota);
                Log.d("FragmentoPublicar", "Imágenes: " + encodedImages.toString());

                Publicacion publicacion = new Publicacion(descripcion, encodedImages, idMascota);
                InsertPublicacion insertPublicacion = new InsertPublicacion();
                String url = "http://" + configIp.IP + ":8000/api/crear_publicacion/";
                insertPublicacion.insertPublicacion(url, requireContext(), publicacion, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String message = jsonObject.getString("message");
                            if (message.equals("1")) {
                                Toast.makeText(requireContext(), "Publicación añadida", Toast.LENGTH_SHORT).show();
                               // redirectToHomeFragment();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });

        selectImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndOpenGallery();
            }
        });

        return view;
    }

    private void checkPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
    }

    private void displaySelectedImages(ArrayList<Uri> imageUris) {
        imageContainer.removeAllViews();
        selectedBitmaps.clear();
        encodedImages.clear(); // Limpiar antes de añadir nuevas imágenes
        for (Uri uri : imageUris) {
            try {
                // Convertir URI a Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                selectedBitmaps.add(bitmap);

                // Crear ImageView y establecer Bitmap
                ImageView imageView = new ImageView(requireContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        450, // Ancho fijo para todas las imágenes, ajusta según sea necesario
                        ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(8, 0, 8, 0);  // Márgenes pequeños entre imágenes
                imageView.setLayoutParams(layoutParams);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Ajusta la imagen sin deformarla
                imageContainer.addView(imageView);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void encodeImages() {
        encodedImages.clear(); // Limpiar la lista de imágenes codificadas antes de añadir nuevas
        for (Bitmap bitmap : selectedBitmaps) {
            // Aquí puedes usar cualquier URI, solo se necesita para obtener el tipo MIME.
            Uri dummyUri = Uri.parse("content://dummyUri");
            String encodedImage = encodeImage(bitmap, dummyUri);
            encodedImages.add(encodedImage);
        }
    }

    private String encodeImage(Bitmap image, Uri imageUri) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String mimeType = requireContext().getContentResolver().getType(imageUri);  // Obtener el tipo MIME del Uri
        Bitmap.CompressFormat compressFormat = mimeType != null && mimeType.equals("image/png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        int quality = mimeType != null && mimeType.equals("image/png") ? 100 : 90;  // PNGs deberían ser comprimidos sin pérdida de calidad
        image.compress(compressFormat, quality, baos);
        byte[] imageBytes = baos.toByteArray();
        String mime = mimeType != null && mimeType.equals("image/png") ? "image/png" : "image/jpeg";
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return "data:" + mime + ";base64," + encodedImage;
    }

    private void redirectToHomeFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.navigation_feed, new FragmentHome());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
