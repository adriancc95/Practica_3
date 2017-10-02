package com.adriancastano.tillapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static String correoR, contrasenaR, correo, contrasena;
    EditText eCorreo, eContrasena;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private int optLog; //0-No hay 1-facebook 2-Google 3-Registro

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 5678;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eCorreo = (EditText) findViewById(R.id.eCorreo);
        eContrasena = (EditText) findViewById(R.id.eContrasena);

        //-----------------------LOGIN CON GOOGLE---------------------------------------------

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

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        //------------------------LOGIN CON FACEBOOK------------------------------------------

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile", "user_photos");

        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()

        {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                optLog = 1;

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        displayUserInfo(object);

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name, last_name, email, picture, id");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

                goMainActivity();

            }


            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error en el l ogin", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayUserInfo(JSONObject object) {
        String first_name = "", last_name = "", email = "", id = "";
        URL url = null;


        try {
            first_name = object.getString("first_name");
            last_name = object.getString("last_name");
            email = object.getString("email");
            id = object.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            url = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString("correo", first_name + " " + last_name);
        editor.putString("contrasena", email);
        editor.putString("url", String.valueOf(url));
        editor.commit();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void goMainActivity() {

        prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        editor = prefs.edit();

        //almacenamos el valor de optLog
        editor.putInt("optLog", optLog);
        editor.commit();

        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        startActivity(intent);
        finish();


    }

    public void iniciar(View view) {

        //se realizan las validaciones, SI SE CUMPLEN:
        correo = eCorreo.getText().toString();
        contrasena = eContrasena.getText().toString();

        prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        editor = prefs.edit();

        if (correo.equals(correoR) && contrasena.equals(contrasenaR)) {
            optLog = 3;
            editor.putString("contrasena", contrasena);
            editor.putString("correo", correo);
            editor.putString("url", "https://www.ibertronica.es/blog/wp-content/uploads/2016/02/perfil-azul.png");
            //editor.putInt("optLog", optLog);
            editor.commit();
            goMainActivity();
        } else {
            Toast.makeText(this, "Usuario y contraseña invalidos", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 1234 && resultCode == RESULT_OK) {//Registro
            correoR = data.getExtras().getString("correo");
            contrasenaR = data.getExtras().getString("contrasena");
            Toast.makeText(this, correoR + contrasenaR, Toast.LENGTH_SHORT).show();
        } else if (requestCode == RC_SIGN_IN) {//Login con google
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {//Login con facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("google", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            optLog = 2;
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            editor = prefs.edit();
            editor.putString("correo", acct.getDisplayName());
            editor.putString("contrasena", acct.getEmail());
            editor.putString("url", String.valueOf(acct.getPhotoUrl()));
            editor.commit();

            Log.d("nombre de usuario", acct.getDisplayName());
            goMainActivity();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getApplicationContext(), "Error en login", Toast.LENGTH_SHORT).show();

        }
    }

    public void registro(View view) {
        Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
        startActivityForResult(intent, 1234);
    }
}