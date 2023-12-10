package com.example.myapplication.Modelo;

public class Usuario {
    private String userName;
    private String password;
    private String fullName;
    private String email;

    public Usuario(String userName, String password, String fullName, String email) {
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public Usuario(String email, String userName, String fullName){
        this.email = email;
        this.userName = userName;
        this.fullName = fullName;
    }

    public Usuario(String email){
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
