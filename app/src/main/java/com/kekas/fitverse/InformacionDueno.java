package com.kekas.fitverse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InformacionDueno extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion_dueno);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        String idUser = intent.getStringExtra("USER_ID");

        TextView nom = findViewById(R.id.infoNombreDueno);
        TextView tel = findViewById(R.id.infoTelDueno);
        TextView use = findViewById(R.id.infoUserDueno);
        TextView fec = findViewById(R.id.infoFechaDueno);
        LinearLayout foto = findViewById(R.id.fotoDueno3);
        obtenerNombreUsuario(idUser, nom, tel, use, fec, foto);
        MaterialCardView info = findViewById(R.id.botonVolverDueno);
        MaterialCardView edit = findViewById(R.id.editarInfo3);
        MaterialCardView salir = findViewById(R.id.botonCerrarSesion3);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformacionDueno.this,DatosDueno.class);
                intent.putExtra("USER_ID", idUser);
                intent.putExtra("TIPO", "Dueno");
                startActivity(intent);
                finish();
            }
        });
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar sesión de Firebase
                mAuth.signOut();

                // Cerrar sesión de Google
                mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                    // Redirigir a MainActivity después de cerrar sesión
                    Intent intent = new Intent(InformacionDueno.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finaliza esta actividad para evitar regresar con el botón de retroceso
                });
            }
        });
    }

    private void obtenerNombreUsuario(String userId, TextView nombreEditText,TextView especEditText, TextView userEditText, TextView fechaEditText, LinearLayout fotoL) {
        db.collection("Dueno").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String foto = document.getString("foto");
                            String fecha = document.getString("registro");
                            String usuario = document.getString("usuario");

                            String telef = document.getString("telefono");

                            if (nombre != null) {
                                nombreEditText.setText(nombre);
                            }
                            if (telef != null) {
                                especEditText.setText(telef);
                            }

                            if (fecha != null) {
                                fechaEditText.setText(fecha);
                            }
                            if (usuario != null) {
                                userEditText.setText(usuario);
                            }

                            if (foto != null) {
                                Glide.with(this)
                                        .load(foto)
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                fotoL.setBackground(resource);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}