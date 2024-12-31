package com.kekas.fitverse;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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

public class ConsultaRutina extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_consulta_rutina);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_RUTINA");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton fl = findViewById(R.id.fabRutinaConsulta);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultaRutina.this, AgregarEjercicio.class);
                intent.putExtra("ID_RUTINA", idRutina);
                startActivity(intent);
                finish();
            }
        });
        gridLayout = findViewById(R.id.gridEjerciciosConsulta);
        TextView nombre = findViewById(R.id.nombreRutinaConsulta);
        TextView enfoque = findViewById(R.id.enfoqueRutinaConsulta);
        TextView descr = findViewById(R.id.descripcionRutinaConsulta);
        TextView duracion = findViewById(R.id.duracionRutinaConsulta);
        LinearLayout ly = findViewById(R.id.fotoRutinaConsulta);
        MaterialCardView borrar = findViewById(R.id.borrarRutina);
        MaterialCardView editar = findViewById(R.id.editarRutina);

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultaRutina.this, EditarRutina.class);
                intent.putExtra("ID_RUTINA", idRutina);
                startActivity(intent);
                finish();
            }
        });

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un AlertDialog de confirmación
                new AlertDialog.Builder(ConsultaRutina.this)
                        .setTitle("Confirmación")
                        .setMessage("¿Estás seguro de que deseas eliminar esta rutina?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceder a eliminar el documento si el usuario confirma
                                db.collection("Coach")
                                        .document(currentUser.getUid())  // Especifica el ID del documento Coach aquí
                                        .collection("Rutinas")
                                        .document(idRutina)  // ID del documento de 'Rutinas' que contiene los ejercicios
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot exerciseDoc = task.getResult();
                                                // Eliminar el documento
                                                exerciseDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                                            //Intent intent = new Intent(ConsultaRutina.this, Coach.class);
                                                            //startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error al eliminar documento", e);
                                                        });
                                            } else {
                                                Log.e("Firestore", "Error al obtener documentos de Ejercicios", task.getException());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error en la operación", e);
                                        });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();  // Mostrar el diálogo
            }
        });

        // Cargar las rutinas en el GridLayout
        cargarInfoRutina(currentUser.getUid(), nombre, enfoque, descr, duracion, ly);
        cargarEjercicios(currentUser.getUid(), idRutina);
        //cargarEjercicios(idRutina);
    }

    private void cargarInfoRutina(String userId, TextView n, TextView en, TextView d, TextView dur, LinearLayout l) {
        db.collection("Coach").document(userId).collection("Rutinas").document(idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        n.setText(document.getString("nombre"));
                        d.setText(document.getString("descripcion"));
                        en.setText(document.getString("enfoque"));
                        dur.setText(document.getString("duracion") + " minutos");
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
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.ejercicios_card, gridLayout, false);

        // Referencias a los elementos dentro de la tarjeta
        TextView nombreRutina = cardView.findViewById(R.id.consultaNombreEjercicio);
        TextView descripcion = cardView.findViewById(R.id.consultaDescripcionEjercicio);
        TextView duracion = cardView.findViewById(R.id.consultaSeriesEjercicio);
        TextView enfoque = cardView.findViewById(R.id.consultaRepeEjercicio);

        LinearLayout fotoRutina = cardView.findViewById(R.id.consultaFotoEjercicio);

        MaterialCardView borrar = cardView.findViewById(R.id.borrarEjercicio);
        MaterialCardView editar = cardView.findViewById(R.id.editarEjercicio);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultaRutina.this, EditarEjercicio.class);
                intent.putExtra("ID_RUTINA", idRutina);
                intent.putExtra("ID_EJERCICIO", document.getId());
                startActivity(intent);
                finish();
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un AlertDialog de confirmación
                    new AlertDialog.Builder(ConsultaRutina.this)
                        .setTitle("Confirmación")
                        .setMessage("¿Estás seguro de que deseas eliminar este ejercicio?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceder a eliminar el documento si el usuario confirma
                                db.collection("Coach")
                                        .document(userId)  // Especifica el ID del documento Coach aquí
                                        .collection("Rutinas")
                                        .document(rutinaId)
                                        .collection("Ejercicios")
                                        .document(document.getId()) // ID del documento de 'Ejercicios' que deseas eliminar
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot exerciseDoc = task.getResult();
                                                // Eliminar el documento de la colección 'Ejercicios'
                                                exerciseDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                                            gridLayout.removeAllViews();  // Limpiar las vistas si es necesario
                                                            cargarEjercicios(userId, rutinaId);  // Recargar los ejercicios después de eliminar
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error al eliminar documento", e);
                                                        });
                                            } else {
                                                Log.e("Firestore", "Error al obtener documento de Ejercicios", task.getException());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error en la operación", e);
                                        });
                            }
                        })
                        .setNegativeButton("No", null)  // Si el usuario presiona "No", no se hace nada
                        .show();  // Mostrar el diálogo
            }
        });

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