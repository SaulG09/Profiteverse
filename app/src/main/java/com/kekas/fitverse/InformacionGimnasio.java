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

public class InformacionGimnasio extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private GridLayout gridLayout;
    private String idRutina;
    private String idDueno;
    private         MaterialCardView inscrito;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion_gimnasio);
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        idDueno = intent.getStringExtra("ID_DUENO");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        gridLayout = findViewById(R.id.gridGymsPlanes3);
        inscrito = findViewById(R.id.inscritoGym);
        //gridLayout = findViewById(R.id.gridOpcionesGymConsulta);
        TextView nombre = findViewById(R.id.nombreGymConsulta3);
        TextView descr = findViewById(R.id.mapaGymConsulta3);
        LinearLayout ly = findViewById(R.id.fotoGymConsulta3);
        cargarRutinas(idRutina);
        cargarInfoRutina( nombre, descr, ly);






    }
    private void cargarInfoRutina(TextView n, TextView en, LinearLayout l) {
        db.collection("Suscripcion")
                .whereEqualTo("id_gym", idRutina).whereEqualTo("id_usuario", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Crear y configurar cada card

                                inscrito.setVisibility(View.VISIBLE);

                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());

        // Usamos el idRutina como el ID del documento en la colección "Gyms"
        db.collection("Dueno").document(idDueno).collection("Gyms") // Colección donde están los gimnasios
                .document(idRutina) // Usamos el idRutina directamente como el ID del documento
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            // Si el documento existe, obtenemos los datos del gimnasio
                            n.setText(document.getString("nombre"));
                            Double latitud = document.getDouble("latitud");
                            Double longitud = document.getDouble("longitud");
                            LatLng latLng = new LatLng(latitud, longitud);

                            en.setText(getLocationName(latLng.latitude, latLng.longitude));

                            Glide.with(this)
                                    .load(document.getString("foto"))
                                    .into(new CustomTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            l.setBackground(resource); // Establece la imagen de fondo
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                            // Maneja la limpieza de recursos si es necesario
                                        }
                                    });
                        } else {
                            // Si el documento no existe, maneja el caso apropiadamente
                            Log.e("Firestore", "El gimnasio con ID " + idRutina + " no existe.");
                        }
                    } else {
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());


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
        db.collection("Dueno").document(idDueno).collection("Gyms").document(idRutina).collection("Planes")
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
        MaterialCardView cardView = (MaterialCardView) getLayoutInflater().inflate(R.layout.plan_user_card, gridLayout, false);

        // Referencias a los elementos dentro de la tarjeta
        TextView nombre = cardView.findViewById(R.id.nombrePlan2);
        TextView descripcion = cardView.findViewById(R.id.descripcionPlan2);
        TextView precio = cardView.findViewById(R.id.precioPlan2);
        TextView duracion = cardView.findViewById(R.id.duracionPlan2);



        // Asignar valores obtenidos de Firestore
        nombre.setText(document.getString("nombre"));
        descripcion.setText(document.getString("descripcion"));
        precio.setText("$" + document.getLong("precio"));
        duracion.setText(document.getLong("duracion") + " días");


        // Cargar la imagen de la rutina usando Glide


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