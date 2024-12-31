package com.kekas.fitverse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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

import org.checkerframework.checker.units.qual.Luminance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConsultarGym extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_consultar_gym);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //gridLayout = findViewById(R.id.gridOpcionesGymConsulta);
        TextView nombre = findViewById(R.id.nombreGymConsulta);
        TextView descr = findViewById(R.id.mapaGymConsulta);
        LinearLayout ly = findViewById(R.id.fotoGymConsulta);
        LinearLayout ly2 = findViewById(R.id.gymFotoFondo);

        MaterialCardView planes = findViewById(R.id.verPlanGym);

        planes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultarGym.this, Planes.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                //finish();
            }
        });

        MaterialCardView borrar = findViewById(R.id.borrarGym);
        MaterialCardView editar = findViewById(R.id.editarGym);

        MaterialCardView coaches = findViewById(R.id.verCoachGym);
        MaterialCardView usuarios = findViewById(R.id.verUsuarioGym);

        usuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultarGym.this, VerAlumnoDueno.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                //finish();
            }
        });

        coaches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultarGym.this, VerCoachDueno.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                //finish();
            }
        });


        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultarGym.this, EditarGym.class);
                intent.putExtra("ID_GYM", idRutina);
                startActivity(intent);
                finish();
            }
        });

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar un cuadro de diálogo para confirmar la acción
                new AlertDialog.Builder(ConsultarGym.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar este gym?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            // Si el usuario confirma, proceder con la eliminación
                            db.collection("Dueno")
                                    .document(currentUser.getUid())  // Especifica el ID del documento Coach aquí
                                    .collection("Gyms")
                                    .document(idRutina)  // ID del documento de 'Rutinas' que contiene los ejercicios
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot exerciseDoc = task.getResult();
                                            // Eliminar el documento
                                            exerciseDoc.getReference().delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("Firestore", "Documento eliminado correctamente.");
                                                        Intent intent = new Intent(ConsultarGym.this, Dueno.class);
                                                        startActivity(intent);
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
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            // Si el usuario cancela, cerrar el diálogo
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        // Cargar las rutinas en el GridLayout
        cargarInfoRutina(currentUser.getUid(), nombre, descr, ly, ly2);
        //cargarEjercicios(currentUser.getUid(), idRutina);
        //cargarEjercicios(idRutina);
    }

    private void cargarInfoRutina(String userId, TextView n, TextView en, LinearLayout l, LinearLayout l2) {
        db.collection("Dueno").document(userId).collection("Gyms").document(idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        n.setText(document.getString("nombre"));
                        LatLng latLng = new LatLng(document.getDouble("latitud"), document.getDouble("longitud"));

                        en.setText(getLocationName(latLng.latitude, latLng.longitude));
                        Glide.with(this)
                                .load(document.getString("foto"))
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        l.setBackground(resource);
                                        Bitmap originalBitmap = ((BitmapDrawable) resource).getBitmap();

// Creamos una matriz para la transformación de inversión vertical
                                        Matrix matrix = new Matrix();
                                        matrix.preScale(1.0f, -1.0f); // Escala de 1 en ancho (sin cambios) y -1 en alto (inversión)

// Creamos un nuevo Bitmap a partir del original con la matriz de transformación
                                        Bitmap invertedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                                                originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

// Convertimos el nuevo Bitmap en un BitmapDrawable
                                        Drawable invertedDrawable = new BitmapDrawable(getResources(), invertedBitmap);

// Aplicamos el Bitmap invertido como fondo
                                        l2.setBackground(invertedDrawable);
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
                Intent intent = new Intent(ConsultarGym.this, EditarEjercicio.class);
                intent.putExtra("ID_RUTINA", idRutina);
                intent.putExtra("ID_EJERCICIO", document.getId());
                startActivity(intent);
                finish();
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Coach")
                        .document(userId)  // Especifica el ID del documento Coach aquí
                        .collection("Rutinas")
                        .document(rutinaId)
                        .collection("Ejercicios")
                        .document(document.getId())// ID del documento de 'Rutinas' que contiene los ejercicios
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot exerciseDoc = task.getResult();
                                // Eliminar cada documento en 'Ejercicios'
                                exerciseDoc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Documento eliminado correctamente.");
                                            gridLayout.removeAllViews();
                                            cargarEjercicios(userId, rutinaId);

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

    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return "Ubicación desconocida";
            }
            Address addressData = addresses.get(0);
            String address = addressData.getAddressLine(0);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return "Ubicación desconocida";
        }
    }
}