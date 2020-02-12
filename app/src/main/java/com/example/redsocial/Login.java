package com.example.redsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    // Vistas
    EditText nEmailEt, nPasswordEt;
    TextView notHaveAccountTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleButtonLogin;

    // Declarar Firebase
    private FirebaseAuth mAuth;

    // Progress Dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Barra de acción y su título
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crer una cuenta");

        // Habilitar los botones
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicialización
        nEmailEt = (EditText) findViewById(R.id.emailEt);
        nPasswordEt =  (EditText) findViewById(R.id.passwordEt);
        notHaveAccountTv = (TextView) findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = (TextView) findViewById(R.id.recoverPassTv);
        mLoginBtn = (Button) findViewById(R.id.loginBtn);
        mGoogleButtonLogin = findViewById(R.id.googleLoginBtn);


        // Botón de Login
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dar datos
                String email = nEmailEt.getText().toString();
                String passw = nPasswordEt.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // Datos introducidos invalidos
                    nEmailEt.setError("Email invalido");
                    nEmailEt.setFocusable(true);
                }
                else {
                    loginUser(email,passw);
                }
            }
        });

        // ¿No tienes cuenta? Click
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, RegistroUsuario.class));
                finish();
            }
        });
        // Recuperar contraseña (click)
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        //  Intent Google Button
        mGoogleButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comenzamos con el acceso con una cuenta de Google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Inicialización del Progress Dialog
        pd = new ProgressDialog(this);

    }

    private void showRecoverPasswordDialog() {

        // Mostar un AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        // Le damos un layout al cuadro de diálogo
        LinearLayout linearLayout = new LinearLayout(this);

        // Vistas para el cuadro de diálogo
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Dimension del EditText
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        // Botón para recuperar contraseña
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Pedimos el email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        // Botón para cancelar
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
            public void onClick(DialogInterface dialogInterface, int which) {

            // Cerrar pestaña de dialogo
            dialogInterface.dismiss();

            }
      });

        // Mostramos el cuadro de dialogo
        builder.create().show();

}

    private void beginRecovery(String email) {
        // Mostar Progress Dialog
        pd.setMessage("Enviando correo...");
        pd.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(Login.this, "Correo enviado." , Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "Error." , Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                // Mostrar el error
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loginUser(String email, String passw) {

        // Mostar Progress Dialog
        pd.setMessage("Entrando...");
        pd.show();

        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Rechazar el progress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // El usuario está logeandose, cargar DashBoard
                            startActivity(new Intent(Login.this, DashBoard.class));
                            finish();

                        } else {
                            // Rechazar el progress dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Rechazar el progress dialog
                pd.dismiss();
                // En caso de error, mostrarlo
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean onSupportNavigateUp() {
        onBackPressed(); // Ir a la actividad anterior
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Si es la primera vez que se logea con Google, mostar informacion
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){

                                // Leemos el email y el id del usuario
                                String email = user.getEmail();
                                String uid = user.getUid();

                                // Cuando el usuario se registra se almacena en la base de datos
                                // Usamos un HashMap
                                HashMap<Object,String> hashMap = new HashMap<>();
                                // Completamos el HashMap con informacion
                                hashMap.put("email" , email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", ""); //add
                                hashMap.put("phone", ""); // add
                                hashMap.put("image",  "");

                                // Instanciamos la base de datos de Firebase
                                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                                // Buscamos los usuarios
                                DatabaseReference reference = database.getReference("Users");
                                // Guardamos los datos en el HashMap
                                reference.child(uid).setValue(hashMap);

                            }

                            // Mostar el correo en un Toast
                            Toast.makeText(Login.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            // Ir a la pestaña de DashBoard
                            startActivity(new Intent(Login.this, DashBoard.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Conexión fallida...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
