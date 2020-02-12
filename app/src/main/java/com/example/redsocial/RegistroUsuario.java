package com.example.redsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistroUsuario extends AppCompatActivity {

    // Vistas
    EditText nEmailEt, nPasswordEt;
    Button nRegisterBtn;
    TextView mHaveAccountTv;

    // Progressbar to display while registering user
    ProgressDialog progessDialog;

    // Declare an instance of FirebaseAutentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        // Barra de acción y su título
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crer una cuenta");


        // Habilitar los botones
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init
        nEmailEt = (EditText) findViewById(R.id.emailEt);
        nPasswordEt = (EditText) findViewById(R.id.passwordEt);
        nRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
        progessDialog = new ProgressDialog(this);
        progessDialog.setMessage("Registering User...");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Handle register btn click
        nRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input email, password
                String email = nEmailEt.getText().toString().trim();
                String password = nPasswordEt.getText().toString().trim();

                // Validación
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    nEmailEt.setError("Invalid Email");
                    nEmailEt.setFocusable(true);

                } else if (password.length() < 6) {

                    nPasswordEt.setError("La contraseña tiene que tener como mínimo seis carácteres.");
                    nPasswordEt.setFocusable(true);

                } else {

                    registerUser(email, password);
                }
            }
        });

        // Login OnClickListener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistroUsuario.this, Login.class));
                finish();
            }
        });

    }

            private void registerUser(String email, String password) {

                progessDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progessDialog.dismiss();

                                    FirebaseUser user = mAuth.getCurrentUser();

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

                                    Toast.makeText(RegistroUsuario.this, "Registrado...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistroUsuario.this, DashBoard.class));
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    progessDialog.dismiss();
                                    Toast.makeText(RegistroUsuario.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }

                                // ...
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progessDialog.dismiss();
                        Toast.makeText(RegistroUsuario.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Ir a la actividad anterior
        return super.onSupportNavigateUp();
    }
}
