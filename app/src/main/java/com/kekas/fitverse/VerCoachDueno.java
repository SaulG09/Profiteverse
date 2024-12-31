package com.kekas.fitverse;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VerCoachDueno extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_coach_dueno);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton fl = findViewById(R.id.fabCoachGym);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        gridLayout = findViewById(R.id.gridCoachesGym);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerCoachDueno.this, AfiliarCoach.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                finish();
            }
        });


        // Cargar las rutinas en el GridLayout
        cargarRutinas();
    }

    private void cargarRutinas() {
        Map<String, Object> entrenamiento = new HashMap<>();
        entrenamiento.put("nombre", "Rutina de Resistencia");
        entrenamiento.put("descripcion", "45 minutos");
        entrenamiento.put("enfoque", "Alta");
        entrenamiento.put("foto", "\"https://firebasestorage.googleapis.com/v0/b/profitverse-bbb73.appspot.com/o/fotos%2Ffoto_1730931375751.jpg?alt=media&token=d4414c3d-a023-46d5-90a3-1734c17a63f4\"\n" +
                "(cadena)\n" +
                "\n");

        // Agregar el nuevo documento
        /*db.collection("Coach").document(userId).collection("Rutinas")
                .add(entrenamiento)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al agregar el documento
                    String idDocumento = documentReference.getId();
                    System.out.println("Documento creado con ID: " + idDocumento);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se agrego", Toast.LENGTH_SHORT).show();

                    // Error al agregar el documento
                    e.printStackTrace();
                });
*/
        db.collection("Cargo")
                .whereEqualTo("id_gym", idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Crear y configurar cada card


                            agregarRutinaAlGrid(document);
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void agregarRutinaAlGrid(DocumentSnapshot document) {
        // Inflar el layout del item_rutina
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.coach_card, gridLayout, false);
        TextView nombre = cardView.findViewById(R.id.nombreCoachGym);
        TextView especializacion = cardView.findViewById(R.id.espCoachGym);
        TextView telefono = cardView.findViewById(R.id.telCoachGym);
        TextView horario = cardView.findViewById(R.id.horarioCoachGym);

        LinearLayout fotoRutina = cardView.findViewById(R.id.fotoCoachGym);

        MaterialCardView editar = cardView.findViewById(R.id.editarCoachGym);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerCoachDueno.this, EditarCargo.class);
                intent.putExtra("ID_CARGO", document.getId());
                startActivity(intent);
                finish();
            }
        });

        // Cargar la imagen de la rutina usando Glide
        MaterialCardView borrar = cardView.findViewById(R.id.borrarCoachGym);
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un AlertDialog de confirmación
                new AlertDialog.Builder(VerCoachDueno.this)
                        .setTitle("Confirmación")
                        .setMessage("¿Estás seguro de que deseas desafiliar este coach?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceder a eliminar el documento si el usuario confirma
                                db.collection("Cargo")
                                        .document(document.getId())  // ID del documento que deseas eliminar
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot exerciseDoc = task.getResult();
                                                // Eliminar el documento de la colección 'Cargo'
                                                exerciseDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                                            gridLayout.removeAllViews();  // Limpiar las vistas si es necesario
                                                            cargarRutinas();  // Recargar las rutinas o vista después de eliminar
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error al eliminar documento", e);
                                                        });
                                            } else {
                                                Log.e("Firestore", "Error al obtener documento de Cargo", task.getException());
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


        String idCoach = document.getString("id_coach");
        db.collection("Coach").document(idCoach)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentCoach = task.getResult();
                        // Crear y configurar cada card
                        nombre.setText(documentCoach.getString("nombre"));
                        especializacion.setText(documentCoach.getString("especializacion"));
                        telefono.setText(documentCoach.getString("telefono"));
                        horario.setText(document.getString("entrada") + " - " + document.getString("salida"));
                        String fotoUrl = documentCoach.getString("foto");
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


                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());

        // Referencias a los elementos dentro de la tarjeta

        // Añadir la tarjeta al GridLayout
        gridLayout.addView(cardView);
    }

}
