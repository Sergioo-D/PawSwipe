//package com.example.myapplication.vista;
// ***** CLASE COMENTADA POR SI EN LUGAR DE EN FRAGMENTPERFIL DENTRO DE HOME QUIERO USAR OTRA ACTIVITY.
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//
//import com.android.volley.Response;
//import com.example.myapplication.DAL.showDataUser;
//import com.example.myapplication.R;
//import com.example.myapplication.Modelo.Usuario;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class ActivityDatosUser extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_datos_user);
//        getSupportActionBar().hide();
//        TextView usuario = findViewById(R.id.tvUser);
//        TextView name = findViewById(R.id.tvName);
//        TextView correo = findViewById(R.id.tvEmail);
//        String et_email = getIntent().getStringExtra("EMAIL");
////        String et_password = getIntent().getStringExtra("CONTRASEÑA");
//
//        if (et_email != null) {
//            Usuario user = new Usuario(et_email);
//            showDataUser data = new showDataUser();
//            data.datosUser("https://uselessutilities.net/ProyetoDAM/getDataUser.php",
//                        ActivityDatosUser.this, user, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    try{
//                        Log.d("Response", response);
//                        JSONObject jsonObject = new JSONObject(response);
//                        String message = jsonObject.getString("message");
//                        if (message.equals("1")) {
//                            String userName = jsonObject.getString("username");
//                            String fullName = jsonObject.getString("fullname");
//                            String userEmail = jsonObject.getString("email");
//                            usuario.setText("Nombre de usuario: " +  userName);
//                            name.setText("Nombre completo: " + fullName);
//                            correo.setText("Correo electrónico: "+ userEmail);
//                        }
//                    } catch (JSONException e) {
//                        Log.e("JSONException", "Error parsing JSON: " + e.getMessage());
//                    }
//
//                }
//
//            });
//
//
//                // Resto del código para enviar los datos al servidor...
//        } else {
//                // Manejo si los valores son nulos
//            Log.e("ActivityDatosUser", "et_email o et_password es nulo");
//        }
//
//
//    }
//}