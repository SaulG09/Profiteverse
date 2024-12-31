package com.kekas.fitverse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IniciarSesion extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iniciar_sesion);

        setupGoogleSignIn();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializa Firestore

        MaterialButton ingreso = findViewById(R.id.ingresarGoogle);
        MaterialButton login = findViewById(R.id.loginCorreo);

        EditText correo = findViewById(R.id.correoIngreso);
        EditText contra = findViewById(R.id.contraIngreso);

        TextView recuperar = findViewById(R.id.recuperarContra);
        recuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IniciarSesion.this, RecuperarContrasena.class);
                startActivity(intent);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = correo.getText().toString().trim();
                String password = contra.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(IniciarSesion.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(IniciarSesion.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Verificar si el usuario está en la colección "Coach"
                                    db.collection("Coach").document(user.getUid())
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    DocumentSnapshot document = task1.getResult();
                                                    if (document.exists()) {
                                                        // Usuario pertenece a la colección "Coach"
                                                        Intent intent = new Intent(IniciarSesion.this, Coach.class);
                                                        intent.putExtra("ID_USER", user.getUid());
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        db.collection("Dueno").document(user.getUid())
                                                                .get()
                                                                .addOnCompleteListener(task2 -> {
                                                                    if (task2.isSuccessful()) {
                                                                        DocumentSnapshot document2 = task2.getResult();
                                                                        if (document2.exists()) {
                                                                            // Usuario pertenece a la colección "Coach"
                                                                            Intent intent = new Intent(IniciarSesion.this, Dueno.class);
                                                                            intent.putExtra("ID_USER", user.getUid());
                                                                            startActivity(intent);
                                                                            finish();
                                                                        } else {
                                                                            db.collection("Usuario").document(user.getUid())
                                                                                    .get()
                                                                                    .addOnCompleteListener(task3 -> {
                                                                                        if (task3.isSuccessful()) {
                                                                                            DocumentSnapshot document3 = task3.getResult();
                                                                                            if (document3.exists()) {
                                                                                                // Usuario pertenece a la colección "Coach"
                                                                                                Intent intent = new Intent(IniciarSesion.this, Usuario.class);
                                                                                                intent.putExtra("ID_USER", user.getUid());
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            } else {
                                                                                                Toast.makeText(IniciarSesion.this, "El usuario no fue encontrado", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        } else {
                                                                                            Toast.makeText(IniciarSesion.this, "Error al verificar la colección", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });                                                                                              }
                                                                    } else {
                                                                        Toast.makeText(IniciarSesion.this, "Error al verificar la colección", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });                                                    }
                                                } else {
                                                                                                }
                                            });

                                }
                            } else {
                                Toast.makeText(IniciarSesion.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        ingreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("60072103296-bu6ekf4lgv2ar3dn7tl8sm4et4imrfm1.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Consultar si el usuario está en la colección "Coach"
                            db.collection("Coach").document(currentUser.getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document.exists()) {
                                                // Usuario pertenece a la colección "Coach"
                                                Intent intent = new Intent(IniciarSesion.this, Coach.class);
                                                intent.putExtra("ID_USER", currentUser.getUid());
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                db.collection("Usuario").document(currentUser.getUid())
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                DocumentSnapshot document2 = task2.getResult();
                                                                if (document2.exists()) {
                                                                    // Usuario pertenece a la colección "Coach"
                                                                    Intent intent = new Intent(IniciarSesion.this, Usuario.class);
                                                                    intent.putExtra("ID_USER", currentUser.getUid());
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    db.collection("Dueno").document(currentUser.getUid())
                                                                            .get()
                                                                            .addOnCompleteListener(task4 -> {
                                                                                if (task4.isSuccessful()) {
                                                                                    DocumentSnapshot document4 = task4.getResult();
                                                                                    if (document4.exists()) {
                                                                                        // Usuario pertenece a la colección "Coach"
                                                                                        Intent intent = new Intent(IniciarSesion.this, Dueno.class);
                                                                                        intent.putExtra("ID_USER", currentUser.getUid());
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    } else {
                                                                                        Toast.makeText(this, "El usuario no es un Coach", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                } else {
                                                                                    Toast.makeText(this, "Error al verificar la colección", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });                                                                }
                                                            } else {
                                                                Toast.makeText(this, "Error al verificar la colección", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });                                            }
                                        } else {
                                            Toast.makeText(this, "Error al verificar la colección", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error de autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}