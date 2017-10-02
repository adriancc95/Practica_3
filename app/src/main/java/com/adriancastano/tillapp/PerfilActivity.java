package com.adriancastano.tillapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import static android.R.attr.data;

public class PerfilActivity extends AppCompatActivity {

    private String correoR, contrasenaR, url;
    TextView tCorreo, tContrasena;
    ImageView iPerfil;

    GoogleApiClient mGoogleApiClient;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private int optLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        tCorreo = (TextView) findViewById(R.id.tCorreo);
        tContrasena = (TextView) findViewById(R.id.tContrasena);
        iPerfil = (ImageView) findViewById(R.id.iPerfil);

        prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        editor = prefs.edit();

        //Bundle extras = getIntent().getExtras();
        correoR = prefs.getString("correo", "");
        contrasenaR = prefs.getString("contrasena", "");
        url = prefs.getString("url", "");
        Glide.with(this).load(url).into(iPerfil);
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
        tCorreo.setText(correoR);
        tContrasena.setText(contrasenaR);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //Toque de acceso dado por google
                // por cierto tiempo, informacion del usuario
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "Error en login", Toast.LENGTH_SHORT).show();
                    }
                }/* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_perfil, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.mPrincipal:
                finish();
                break;
            case R.id.mCerrar:
                prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                editor = prefs.edit();

                optLog = prefs.getInt("optLog",0);

                if (optLog == 1) {
                    LoginManager.getInstance().logOut();

                } else if (optLog == 2) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    // ...
                                }
                            });
                }
                //almacenamos el valor de optLog
                editor.putInt("optLog", 0);
                editor.commit();

                intent = new Intent(PerfilActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
