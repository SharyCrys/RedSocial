package com.example.redsocial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Vistas
    Button BotonRegistro, BotonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializar vistas
        BotonRegistro = (Button)findViewById(R.id.register_btn);
        BotonLogin = (Button) findViewById(R.id.login_btn);

        BotonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ir a la pestaña de registro
                startActivity(new Intent(MainActivity.this, RegistroUsuario.class));
            }
        });

    }
}
