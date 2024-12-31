package com.kekas.fitverse;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

public class VerAlumnoDueno extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;
    Spinner estado;

    FirebaseUser currentUser;
    String nombre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_alumno_dueno);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton fl = findViewById(R.id.fabUsuarioGym);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        gridLayout = findViewById(R.id.gridUsuariosGym);
        estado = findViewById(R.id.spinnerEstado);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Todos", "Activo", "Expirado"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        estado.setAdapter(adapter);
        // Configurar el Spinner con un listener
        estado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filtro = parent.getItemAtPosition(position).toString();
                gridLayout.removeAllViews(); // Limpiar las vistas del GridLayout
                cargarRutinas(filtro); // Recargar las rutinas con el filtro seleccionado
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Por defecto mostrar todos si no hay selección
                cargarRutinas("Todos");
            }
        });

        // Botón flotante para agregar usuarios
        fl.setOnClickListener(v -> {
            Intent intent1 = new Intent(VerAlumnoDueno.this, AfiliarUsuario.class);
            intent1.putExtra("ID_GYM", idRutina);
            startActivity(intent1);
            finish();
        });

        // Cargar inicialmente todas las rutinas
    }


    private void cargarRutinas(String filtro) {
        db.collection("Suscripcion")
                .whereEqualTo("id_gym", idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String estadoSuscripcion = document.getString("estado");

                            // Filtrar según el estado seleccionado
                            if (filtro.equals("Todos") || filtro.equals(estadoSuscripcion)) {
                                agregarRutinaAlGrid(document);
                            }
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
                Intent intent = new Intent(VerAlumnoDueno.this, EditarSuscripcion.class);
                intent.putExtra("ID_SUS", document.getId());
                intent.putExtra("ID_GYM", idRutina);

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
                new AlertDialog.Builder(VerAlumnoDueno.this)
                        .setTitle("Confirmación")
                        .setMessage("¿Estás seguro de que deseas eliminar esta suscripción?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Proceder a eliminar el documento si el usuario confirma
                                db.collection("Suscripcion")
                                        .document(document.getId())  // ID del documento de 'Suscripcion'
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot exerciseDoc = task.getResult();
                                                // Eliminar el documento
                                                exerciseDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                                            gridLayout.removeAllViews();
                                                            estado.setSelection(0);// Limpiar las vistas si es necesario
                                                            cargarRutinas("Todos");  // Recargar las rutinas o vista
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error al eliminar documento", e);
                                                        });
                                            } else {
                                                Log.e("Firestore", "Error al obtener documentos de Suscripcion", task.getException());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error en la operación", e);
                                        });
                            }
                        })
                        .setNegativeButton("No", null)  // Si el usuario no confirma, no se hace nada
                        .show();  // Mostrar el diálogo
            }
        });


        String idCoach = document.getString("id_usuario");
        db.collection("Usuario").document(idCoach)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentCoach = task.getResult();
                        // Crear y configurar cada card
                        nombre.setText(documentCoach.getString("nombre"));
                        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina).collection("Planes").document(document.getString("id_plan"))
                                .get()
                                .addOnCompleteListener(task4 -> {
                                    if (task4.isSuccessful()) {
                                        DocumentSnapshot document2 = task4.getResult();
                                        especializacion.setText((document2.getString("nombre")));

                                    }
                                })
                                .addOnFailureListener(e -> e.printStackTrace());
                        //especializacion.setText(obtenerPlan(document.getString("id_plan")));
                       telefono.setText(document.getString("estado"));
                       if(document.getString("estado").equals("Activo")){
                           telefono.setTypeface(null, Typeface.BOLD);
                           telefono.setTextColor(Color.parseColor("#8BC34A"));
                       }else{
                           telefono.setTypeface(null, Typeface.BOLD);
                           telefono.setTextColor(Color.parseColor("#E91E63"));
                       }
                        horario.setText(document.getString("inicio") + " - " + document.getString("termino"));
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

    private String obtenerPlan(String plan){
        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina).collection("Planes").document(plan)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentCoach = task.getResult();
                        nombre = documentCoach.getString("nombre");
                        // Crear y configurar cada card




                    }else{

                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
        return nombre;
    }

}
