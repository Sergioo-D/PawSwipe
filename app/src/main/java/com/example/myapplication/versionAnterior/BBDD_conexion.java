//package com.example.myapplication;
// ***** CALSE COMENTA, VERSION ANTERIOR, CONEXION JDBC
//import android.content.Context;
//import android.widget.TableLayout;
//import android.widget.Toast;
//
//import java.io.Serializable;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.sql.*;
//
//public class BBDD_conexion {
//    private Connection conexio;
//
//
//    private String url = "jdbc:postgresql://%s:%d/%s";
//    private final String host = "instanciaproyecto.cqj6xncvyxbs.eu-north-1.rds.amazonaws.com";
//    private final String database = "postgres";
//    private final int port = 5432;
//    private final String user = "administrador";
//    private final String pass = "12345678";
//
//
//    public Connection conectar(Context context) throws SQLException, ClassNotFoundException {
//        try {
//            url = String.format(url, host, port, database);
//
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Class.forName("org.postgresql.Driver");
//                        conexio = DriverManager.getConnection(url, user, pass);
//                        for (int i = 0; i < 20; i++) {
//                            System.out.println("conectado verdad?");
//                        }
//
//
//                    } catch (Exception ex) {
//                        for (int i = 0; i < 20; i++) {
//                            System.out.println("No va");
//                        }
//                        System.out.println(ex.getMessage());
//                        System.out.println("--------------------");
//                        System.out.println(ex.getCause());
//                    }
//                }
//            });
//            thread.start();
//            try {
//                thread.join();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        } catch (Exception e){
//            Toast.makeText(context, "a", Toast.LENGTH_SHORT).show();
//        } return conexio;
//    }
//
//    public void desconectar() throws SQLException {
//        if (conexio != null) {
//            conexio.close();
//        }
//    }
//
//    public void insertar(String password, boolean is_superuser, String username, String first_name, String last_name, String email, boolean is_staff, boolean is_active, Timestamp date_joined)  {
//        new Thread(() -> {
//        String insert = "insert into auth_user(password, is_superuser, username, first_name, last_name, email, is_staff, is_active, date_joined)" + "values(?,?,?,?,?,?,?,?,?);";
//        try {
//            PreparedStatement st = conexio.prepareStatement(insert);
//            st.setString(1, password);
//            st.setBoolean(2, is_superuser);
//            st.setString(3, username);
//            st.setString(4, first_name);
//            st.setString(5, last_name);
//            st.setString(6, email);
//            st.setBoolean(7, is_staff);
//            st.setBoolean(8, is_active);
//            st.setTimestamp(9, date_joined);
//            st.executeUpdate();
//            st.close();
//            System.out.println("registrado");
//
//
//        }catch (SQLException e){
//            e.printStackTrace();
//            System.out.println("No registrado");
//            }
//    }).start();
//
//    }
//
//
//    public static String encryptPassword(String password) {
//        try {
//            // Crea un objeto MessageDigest con el algoritmo SHA-256
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//
//            // Convierte la contraseña en un arreglo de bytes
//            byte[] passwordBytes = password.getBytes();
//
//            // Actualiza el MessageDigest con los bytes de la contraseña
//            md.update(passwordBytes);
//
//            // Calcula el hash
//            byte[] hashBytes = md.digest();
//
//            // Convierte el hash en una representación hexadecimal
//            StringBuilder hexString = new StringBuilder();
//            for (byte hashByte : hashBytes) {
//                String hex = Integer.toHexString(0xff & hashByte);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//}
//
//
//
//
//
//
//
