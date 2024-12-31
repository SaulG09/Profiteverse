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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan en 0
        int currentYear = calendar.get(Calendar.YEAR);
        String currentDate = currentDay + "/" + currentMonth + "/" + currentYear;

// Consultar todos los documentos en la colección "Suscripcion"
        db.collection("Suscripcion")
                .get()
                .addOnCompleteListener(task4 -> {
                    if (task4.isSuccessful()) {
                        for (QueryDocumentSnapshot document2 : task4.getResult()) {
                            // Obtener el valor del campo "termino"
                            String termino = document2.getString("termino");

                            // Verificar si la fecha actual coincide con la fecha de "termino"
                            if (currentDate.equals(termino)) {
                                // Actualizar el campo "estado" a "Inactivo"
                                document2.getReference().update("estado", "Expirada")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Actualización", "Estado actualizado a Inactivo para documento: " + document2.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("ErrorActualización", "Error al actualizar estado", e);
                                        });
                                document2.getReference().update("inicio", "")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Actualización", "Estado actualizado a Inactivo para documento: " + document2.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("ErrorActualización", "Error al actualizar estado", e);
                                        });
                                document2.getReference().update("termino", "")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Actualización", "Estado actualizado a Inactivo para documento: " + document2.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("ErrorActualización", "Error al actualizar estado", e);
                                        });
                            }
                        }
                    } else {
                        Log.w("ErrorConsulta", "Error al obtener documentos", task4.getException());
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());

        if(currentUser != null) {
            db.collection("Coach").document(currentUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // El usuario está en la colección "Coach"
                    Intent intent = new Intent(MainActivity.this, Coach.class);
                    intent.putExtra("ID_USER", currentUser.getUid());
                    startActivity(intent);
                    finish();
                } else {
                    // Si no está en "Coach", verifica en la colección "Dueño"
                    db.collection("Dueno").document(currentUser.getUid()).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful() && task2.getResult().exists()) {
                            // El usuario está en la colección "Dueño"
                            Intent intent = new Intent(MainActivity.this, Dueno.class);
                            intent.putExtra("ID_USER", currentUser.getUid());
                            startActivity(intent);
                            finish();
                        } else {
                            db.collection("Usuario").document(currentUser.getUid()).get().addOnCompleteListener(task3 -> {
                                if (task3.isSuccessful() && task3.getResult().exists()) {
                                    // El usuario está en la colección "Dueño"
                                    Intent intent = new Intent(MainActivity.this, Usuario.class);
                                    intent.putExtra("ID_USER", currentUser.getUid());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // El usuario no está en ninguna colección
                                    Toast.makeText(MainActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                                    // Aquí puedes redirigir a una actividad de registro o mostrar un mensaje
                                }
                            }).addOnFailureListener(e -> {

                            });

                        }
                    }).addOnFailureListener(e -> {

                    });
                }
            }).addOnFailureListener(e -> {

            });

        }
        MaterialButton mi = findViewById(R.id.botonInicio);
        mi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IniciarSesion.class);
                startActivity(intent);
                finish();
            }
        });

        MaterialButton mb = findViewById(R.id.botonRegistro);
        mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Registro.class);
                startActivity(intent);
            }
        });

    }

    private void setupGoogleSignIn() {
        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("60072103296-bu6ekf4lgv2ar3dn7tl8sm4et4imrfm1.apps.googleusercontent.com") // Asegúrate de que este sea el ID correcto
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Aquí puedes agregar los botones de inicio de sesión y registrar, como antes.
    }

    @Override
    public void onBackPressed() {
        // No llamamos a super.onBackPressed() para evitar volver a la actividad anterior
        // Puedes mostrar un mensaje o realizar otra acción si lo deseas
        super.onBackPressed();
    }
}