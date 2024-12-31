package com.kekas.fitverse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.Luminance;

import java.util.HashMap;
import java.util.Map;

public class ConsultaRutinaUsuario extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;
    private String idGym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_consulta_rutina_usuario);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_RUTINA");
        idGym = intent.getStringExtra("ID_COACH");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        gridLayout = findViewById(R.id.gridEjerciciosConsultaUsuario);
        TextView nombre = findViewById(R.id.nombreRutinaConsultaUsuario);
        TextView enfoque = findViewById(R.id.enfoqueRutinaConsultaUsuario);
        TextView descr = findViewById(R.id.descripcionRutinaConsultaUsuario);
        TextView duracion = findViewById(R.id.duracionRutinaConsultaUsuario);
        LinearLayout ly = findViewById(R.id.fotoRutinaConsultaUsuario);



        // Cargar las rutinas en el GridLayout
        cargarInfoRutina(idGym, nombre, enfoque, descr, duracion, ly);
        cargarEjercicios(idGym, idRutina);
        //cargarEjercicios(idRutina);
    }

    private void cargarInfoRutina(String userId, TextView n, TextView en, TextView d, TextView dur, LinearLayout l) {
        db.collection("Coach").document(userId).collection("Rutinas").document(idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        n.setText(document.getString("nombre"));
                        d.setText(document.getString("enfoque"));
                        en.setText(document.getString("descripcion"));
                        dur.setText(document.getString("duracion"));
                        Glide.with(this)
                                .load(document.getString("foto"))
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        l.setBackground(resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void cargarEjercicios(String userId, String rutinaId) {
        db.collection("Coach").document(userId).collection("Rutinas").document(rutinaId).collection("Ejercicios")
                .whereEqualTo("id_rutina", rutinaId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Crear y configurar cada card


                            agregarRutinaAlGrid(userId, rutinaId, document);
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void agregarRutinaAlGrid(String userId, String rutinaId, DocumentSnapshot document) {
        // Inflar el layout del item_rutina
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.ejercicios_user_card, gridLayout, false);

        // Referencias a los elementos dentro de la tarjeta
        TextView nombreRutina = cardView.findViewById(R.id.consultaNombreEjercicioUser);
        TextView descripcion = cardView.findViewById(R.id.consultaDescripcionEjercicioUser);
        TextView duracion = cardView.findViewById(R.id.consultaSeriesEjercicioUser);
        TextView enfoque = cardView.findViewById(R.id.consultaRepeEjercicioUser);

        LinearLayout fotoRutina = cardView.findViewById(R.id.consultaFotoEjercicioUser);


        // Asignar valores obtenidos de Firestore
        nombreRutina.setText(document.getString("nombre"));
        descripcion.setText(document.getString("descripcion"));
        enfoque.setText("Repeticiones: " + document.getString("repeticiones"));
        duracion.setText("Series: " + document.getString("series"));

        // Cargar la imagen de la rutina usando Glide
        String fotoUrl = document.getString("foto");
        if (fotoUrl != null) {
            Glide.with(this)
                    .load(fotoUrl)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            fotoRutina.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        // Añadir la tarjeta al GridLayout
        gridLayout.addView(cardView);
    }
}