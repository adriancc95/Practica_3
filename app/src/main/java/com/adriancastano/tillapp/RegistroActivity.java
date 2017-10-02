package com.adriancastano.tillapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    private String correo, contrasena, contrasena2;
    EditText eCorreo, eContrasena, eRepContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        eCorreo = (EditText) findViewById(R.id.eCorreo);
        eContrasena = (EditText) findViewById(R.id.eContrasena);
        eRepContrasena = (EditText) findViewById(R.id.eRepContrasena);
    }

    public void registrar(View view) {
        correo = eCorreo.getText().toString();
        contrasena = eContrasena.getText().toString();
        contrasena2 = eRepContrasena.getText().toString();
        if (correo.equals("") || contrasena.equals("") || contrasena2.equals("")) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
        } else if (!validarEmail(correo)) {
            eCorreo.setError("Email no válido");
        } else if (contrasena.equals(contrasena2)) {
            Intent intent = new Intent();
            intent.putExtra("correo", correo);
            intent.putExtra("contrasena", contrasena);
            setResult(RESULT_OK, intent);
            Toast.makeText(this, "Registro realizado", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}
