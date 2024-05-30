package com.example.myapplication.vista.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.myapplication.ConfigIp;
import com.example.myapplication.DAL.DeleteMascota;
import com.example.myapplication.DAL.UpdateMascota;
import com.example.myapplication.Modelo.Mascota;
import com.example.myapplication.Modelo.PerfilCompletoResponse;
import com.example.myapplication.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ActivityModificar extends AppCompatActivity {

    ConfigIp configIp = new ConfigIp();
    private ActionBar toolbar;
    private Uri selectedImageUri;
    private Mascota mascota; // Hacer que sea miembro de la clase para que pueda ser usado en otros métodos
    private ImageView fotoPerfil;

    private ActivityResultLauncher<String> galleryLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar);
        toolbar = getSupportActionBar();
        toolbar.setTitle("Editar perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button update = findViewById(R.id.bUpdate);
        Button delete = findViewById(R.id.bDelete);  // Nuevo botón
        EditText nombre = findViewById(R.id.etnombre);
        EditText descripcion = findViewById(R.id.etdescripcion);
        fotoPerfil = findViewById(R.id.foto_perfil);
        mascota = (Mascota) getIntent().getSerializableExtra("mascota");
        nombre.setText(mascota.getNombre());
        descripcion.setText(mascota.getDescripcion());
        String imageUrl = "http://" + configIp.IP + ":8000" + mascota.getPerfil().getFotoPerfil();
        Picasso.get().load(imageUrl).placeholder(R.drawable.paw_solid).into(fotoPerfil);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                fotoPerfil.setImageURI(selectedImageUri);
            }
        });

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevoNombre = nombre.getText().toString();
                String nuevaDescripcion = descripcion.getText().toString();
                Bitmap image = null;
                String imagen = null;
                if (selectedImageUri != null) {
                    try {
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mascota.setNombre(nuevoNombre);
                mascota.setDescripcion(nuevaDescripcion);
                if (image != null) {
                    imagen = encodeImage(image, selectedImageUri);
                }
                UpdateMascota update = new UpdateMascota();
                update.modificarMascotaPerfil("http://" + configIp.IP + ":8000/api/actualizar_mascota/", ActivityModificar.this, mascota, imagen, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("Response", response);
                            JSONObject jsonObject = new JSONObject(response);
                            String message = jsonObject.getString("message");
                            if (message.equals("1")) {
                                Log.d("RESPONSE_TAG", "Response from server: " + response);
                                Toast.makeText(ActivityModificar.this, "Datos actualizados", Toast.LENGTH_SHORT).show();

                                // Actualizar datos de la mascota con los datos recibidos del servidor
                                JSONObject perfilActualizado = jsonObject.getJSONObject("perfil");
                                mascota.setNombre(perfilActualizado.getString("nombre"));
                                mascota.setDescripcion(perfilActualizado.getString("descripcion"));
                                mascota.getPerfil().setFotoPerfil(perfilActualizado.getString("fotoPerfil"));

                                // Devolver los datos actualizados a la actividad anterior
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("mascotaActualizada", mascota);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Log.d("RESPONSE_TAG", "Response from server: " + response);
                                Toast.makeText(ActivityModificar.this, "No se han podido guardar los cambios", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
                        }
                    }


                });
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ActivityModificar.this)
                        .setTitle("Eliminar Mascota")
                        .setMessage("¿Estás seguro de que deseas eliminar esta mascota?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Log cuando el usuario confirma la eliminación
                                Log.d("DeleteProcess", "El usuario confirmó la eliminación de la mascota con ID: " + mascota.getId());

                                DeleteMascota deleteMascota = new DeleteMascota();
                                deleteMascota.eliminarMascota("http://" + configIp.IP + ":8000/api/delete_mascota/", ActivityModificar.this, mascota, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            Log.d("Response Delete", "Respuesta recibida del servidor: " + response);
                                            JSONObject jsonObject = new JSONObject(response);
                                            String message = jsonObject.getString("message");
                                            if (message.equals("1")) {
                                                Log.d("RESPONSE_TAG_DELETE_1", "Mascota eliminada exitosamente, response: " + response);
                                                Toast.makeText(ActivityModificar.this, "Mascota eliminada", Toast.LENGTH_SHORT).show();

                                                // Obtener los datos actualizados de la API de perfil_completo
                                                JSONObject perfilActualizado = jsonObject.getJSONObject("perfil");
                                                Gson gson = new Gson();
                                                PerfilCompletoResponse perfilMascotaActual = gson.fromJson(perfilActualizado.toString(), PerfilCompletoResponse.class);

                                                // Devolver los datos actualizados a la actividad anterior
                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtra("nueva_mascota_actual", perfilMascotaActual);
                                                setResult(RESULT_OK, resultIntent);
                                                finish();
                                            } else {
                                                Log.d("RESPONSE_TAG_DELETE_ELSE", "No se pudo eliminar la mascota, response: " + response);
                                                Toast.makeText(ActivityModificar.this, "No se ha podido eliminar la mascota", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            Log.e("JSONException", "Error al parsear JSON: " + e.getMessage());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Log para errores de Volley
                                        Log.e("Volley Error", "Error en la solicitud de eliminación: " + error.toString());
                                        Toast.makeText(ActivityModificar.this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }


    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Manejar el botón de retroceso
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String encodeImage(Bitmap image, Uri imageUri) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String mimeType = getContentResolver().getType(imageUri);  // Obtener el tipo MIME del Uri
        Bitmap.CompressFormat compressFormat = mimeType != null && mimeType.equals("image/png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        int quality = mimeType != null && mimeType.equals("image/png") ? 100 : 90;  // PNGs deberían ser comprimidos sin pérdida de calidad
        image.compress(compressFormat, quality, baos);
        byte[] imageBytes = baos.toByteArray();
        String mime = mimeType != null && mimeType.equals("image/png") ? "image/png" : "image/jpeg";
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        return "data:" + mime + ";base64," + encodedImage;
    }
}