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

public class Planes extends AppCompatActivity {
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
        setContentView(R.layout.activity_planes);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        FloatingActionButton fl = findViewById(R.id.fabGymPlan);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Planes.this, AgregarPlan.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                finish();
            }
        });
        gridLayout = findViewById(R.id.gridGymsPlanes);

        // Cargar las rutinas en el GridLayout
        cargarRutinas(currentUser.getUid());
    }

    private void cargarRutinas(String userId) {
        Map<String, Object> entrenamiento = new HashMap<>();
        entrenamiento.put("id_coach", userId);
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
        db.collection("Dueno").document(userId).collection("Gyms").document(idRutina).collection("Planes")
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
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.plan_card, gridLayout, false);

        MaterialCardView borrar = cardView.findViewById(R.id.borrarPlan);
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un AlertDialog para confirmar la eliminación
                new AlertDialog.Builder(Planes.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar este plan?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceder con la eliminación si el usuario confirma
                                db.collection("Dueno")
                                        .document(currentUser.getUid())
                                        .collection("Gyms")
                                        .document(idRutina)
                                        .collection("Planes")
                                        .document(document.getId()) // ID del documento a eliminar
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot exerciseDoc = task.getResult();
                                                // Eliminar el documento
                                                exerciseDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                                            gridLayout.removeAllViews();
                                                            cargarRutinas(currentUser.getUid());
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
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario cancela la operación, simplemente cerramos el diálogo
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });


        MaterialCardView editar = cardView.findViewById(R.id.editarPlan);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Planes.this, EditarPlan.class);
                intent.putExtra("ID_GYM", idRutina);
                intent.putExtra("ID_PLAN", document.getId());
                startActivity(intent);
                finish();
            }
        });
        // Referencias a los elementos dentro de la tarjeta
        TextView nombre = cardView.findViewById(R.id.nombrePlan);
        TextView descripcion = cardView.findViewById(R.id.descripcionPlan);
        TextView precio = cardView.findViewById(R.id.precioPlan);
        TextView duracion = cardView.findViewById(R.id.duracionPlan);



        // Asignar valores obtenidos de Firestore
        nombre.setText(document.getString("nombre"));
        descripcion.setText(document.getString("descripcion"));
        precio.setText("$" + document.getLong("precio"));
        duracion.setText(document.getLong("duracion") + " días");


        // Cargar la imagen de la rutina usando Glide


        // Añadir la tarjeta al GridLayout
        gridLayout.addView(cardView);
    }


}
