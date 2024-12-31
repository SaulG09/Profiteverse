package com.kekas.fitverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SeleccionUsuario extends AppCompatActivity {

    // Instancia de FirebaseFirestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seleccion_usuario);

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();

        // Obtén el ID del usuario de Firebase del Intent
        Intent intent = getIntent();
        String idUser = intent.getStringExtra("USER_ID");
        Toast.makeText(this, idUser, Toast.LENGTH_SHORT).show();

        // Obtén el usuario autenticado actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Llama a agregarUsuarioAFirestore si el usuario está autenticado

        // Configura el botón de selección de coach
        MaterialCardView mc = findViewById(R.id.botonSeleccionCoach);
        mc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    agregarUsuarioAFirestore(currentUser);
                    Intent intent = new Intent(SeleccionUsuario.this, DatosCoach.class);
                    intent.putExtra("USER_ID", idUser); // Envía el ID del usuario
                    intent.putExtra("TIPO", "Coach");
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("Firestore", "No se encontró un usuario autenticado");
                }

            }
        });

        MaterialCardView mc2 = findViewById(R.id.botonSeleccionDueño);
        mc2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    agregarUsuarioAFirestore2(currentUser);
                    Intent intent = new Intent(SeleccionUsuario.this, DatosDueno.class);
                    intent.putExtra("USER_ID", idUser); // Envía el ID del usuario
                    intent.putExtra("TIPO", "Dueno");
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("Firestore", "No se encontró un usuario autenticado");
                }

            }
        });

        MaterialCardView mc3 = findViewById(R.id.botonSeleccionUsuario);
        mc3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    agregarUsuarioAFirestore3(currentUser);
                    Intent intent = new Intent(SeleccionUsuario.this, DatosUsuario.class);
                    intent.putExtra("USER_ID", idUser); // Envía el ID del usuario
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("Firestore", "No se encontró un usuario autenticado");
                }

            }
        });
    }

    private void agregarUsuarioAFirestore(FirebaseUser user) {
        String userId = user.getUid();

        // Crear un mapa para almacenar la información del usuario
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("usuario", user.getEmail());
        usuarioMap.put("nombre", user.getDisplayName());
        usuarioMap.put("telefono", user.getPhoneNumber());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        usuarioMap.put("registro", fechaActual);

        usuarioMap.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Guardar el usuario en Firestore
        db.collection("Coach").document(userId)
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario registrado con éxito en Firestore"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error al registrar usuario en Firestore", e));

        // Log adicional para verificar la información del usuario
        Log.d("Firestore", "Usuario UID: " + user.getUid());
        Log.d("Firestore", "Email: " + user.getEmail());
        Log.d("Firestore", "DisplayName: " + user.getDisplayName());
        Log.d("Firestore", "Phone: " + user.getPhoneNumber());
        Log.d("Firestore", "Photo URL: " + (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "No photo"));
    }

    private void agregarUsuarioAFirestore2(FirebaseUser user) {
        String userId = user.getUid();

        // Crear un mapa para almacenar la información del usuario
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("usuario", user.getEmail());
        usuarioMap.put("nombre", user.getDisplayName());
        usuarioMap.put("telefono", user.getPhoneNumber());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        usuarioMap.put("registro", fechaActual);

        usuarioMap.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Guardar el usuario en Firestore
        db.collection("Dueno").document(userId)
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario registrado con éxito en Firestore"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error al registrar usuario en Firestore", e));

        // Log adicional para verificar la información del usuario
        Log.d("Firestore", "Usuario UID: " + user.getUid());
        Log.d("Firestore", "Email: " + user.getEmail());
        Log.d("Firestore", "DisplayName: " + user.getDisplayName());
        Log.d("Firestore", "Phone: " + user.getPhoneNumber());
        Log.d("Firestore", "Photo URL: " + (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "No photo"));
    }

    private void agregarUsuarioAFirestore3(FirebaseUser user) {
        String userId = user.getUid();

        // Crear un mapa para almacenar la información del usuario
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("usuario", user.getEmail());
        usuarioMap.put("nombre", user.getDisplayName());
        usuarioMap.put("telefono", user.getPhoneNumber());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        usuarioMap.put("registro", fechaActual);

        usuarioMap.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Guardar el usuario en Firestore
        db.collection("Usuario").document(userId)
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario registrado con éxito en Firestore"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error al registrar usuario en Firestore", e));

        // Log adicional para verificar la información del usuario
        Log.d("Firestore", "Usuario UID: " + user.getUid());
        Log.d("Firestore", "Email: " + user.getEmail());
        Log.d("Firestore", "DisplayName: " + user.getDisplayName());
        Log.d("Firestore", "Phone: " + user.getPhoneNumber());
        Log.d("Firestore", "Photo URL: " + (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "No photo"));
    }
}
