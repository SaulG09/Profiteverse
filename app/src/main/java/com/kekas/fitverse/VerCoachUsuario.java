package com.kekas.fitverse;

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
import com.google.android.material.button.MaterialButton;
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

public class VerCoachUsuario extends AppCompatActivity {
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
        setContentView(R.layout.activity_ver_coach_usuario);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_USER");


        gridLayout = findViewById(R.id.gridCoachesGymUsuario);


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
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.coach_user_card, gridLayout, false);
        TextView nombre = cardView.findViewById(R.id.nombreCoachGymUser);
        TextView especializacion = cardView.findViewById(R.id.espCoachGymUser);
        TextView telefono = cardView.findViewById(R.id.telCoachGymUser);
        TextView horario = cardView.findViewById(R.id.horarioCoachGymUser);

        LinearLayout fotoRutina = cardView.findViewById(R.id.fotoCoachGymUser);
        MaterialButton boton = cardView.findViewById(R.id.rutinasCoachUser);
        String idCoach = document.getString("id_coach");

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VerCoachUsuario.this,VerRutinasUsuario.class);
                intent.putExtra("ID_COACH",idCoach);
                startActivity(intent);
                //finish();
            }
        });


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
