package com.kekas.fitverse;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("60072103296-bu6ekf4lgv2ar3dn7tl8sm4et4imrfm1.apps.googleusercontent.com") // Asegúrate de que este sea el ID correcto
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText correo = findViewById(R.id.textoCorreo);
        EditText contra = findViewById(R.id.textoContra);
        EditText confcontra = findViewById(R.id.textoConfirmarCon);


        MaterialButton mr = findViewById(R.id.registrarCuenta);
        mr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = correo.getText().toString().trim();
                String password = contra.getText().toString().trim();
                String confirmPassword = confcontra.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(Registro.this, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!esPasswordValido(password)) {
                    Toast.makeText(Registro.this, "La contraseña debe tener al menos 10 caracteres, incluir un número y una letra mayúscula.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Deshabilitar el botón para evitar múltiples clics
                mr.setEnabled(false);

                // Verificar si el correo ya está registrado
                verificarCorreoEnFirestore(email, () -> {
                    // Crear cuenta si no está registrada
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        Intent intent = new Intent(Registro.this, SeleccionUsuario.class);
                                        intent.putExtra("USER_ID", user.getUid());
                                        intent.putExtra("TIPO", "Coach");
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Log.w(TAG, "Error al registrar usuario", task.getException());
                                    Toast.makeText(Registro.this, "Error al registrar usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    mr.setEnabled(true);
                                }
                            });
                }, () -> {
                    // Correo ya registrado
                    Toast.makeText(Registro.this, "Este correo ya está registrado.", Toast.LENGTH_SHORT).show();
                    mr.setEnabled(true);
                });
            }
        });



        MaterialButton mgoogle = findViewById(R.id.botonRegistroCoach);
        mgoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

    }

    private boolean esPasswordValido(String password) {
        if (password.length() < 10) {
            return false;
        }
        boolean tieneNumero = false;
        boolean tieneMayuscula = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                tieneNumero = true;
            }
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            }
            if (tieneNumero && tieneMayuscula) {
                return true;
            }
        }
        return false;
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultados del inicio de sesión de Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Inicio de sesión exitoso, autenticar con Firebase
                firebaseAuthWithGoogle(account); // Asegúrate de llamar a este método aquí
            } catch (ApiException e) {
                Log.w("Google SignIn", "Error en el inicio de sesión de Google", e);
                Toast.makeText(this, "Error en el inicio de sesión. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autenticación exitosa, obtener el usuario actual
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FirebaseAuth", "Usuario autenticado: " + user.getEmail());
                        if (user != null) {
                            verificarUsuarioEnFirestore(user); // Verifica si el usuario ya está registrado
                        }
                    } else {
                        Log.w("Google SignIn", "Error en la autenticación con Firebase", task.getException());
                    }
                });
    }

    private void verificarUsuarioEnFirestore(FirebaseUser user) {
        String userEmail = user.getEmail();

        // Verificar en la colección "Coach"
        db.collection("Coach")
                .whereEqualTo("usuario", userEmail)
                .get()
                .addOnCompleteListener(taskCoach -> {
                    if (taskCoach.isSuccessful() && taskCoach.getResult() != null && !taskCoach.getResult().isEmpty()) {
                        // Usuario encontrado en "Coach"
                        redirigirUsuarioRegistrado(userEmail);
                    } else {
                        // Verificar en la colección "Dueno"
                        db.collection("Dueno")
                                .whereEqualTo("usuario", userEmail)
                                .get()
                                .addOnCompleteListener(taskDueno -> {
                                    if (taskDueno.isSuccessful() && taskDueno.getResult() != null && !taskDueno.getResult().isEmpty()) {
                                        // Usuario encontrado en "Dueno"
                                        redirigirUsuarioRegistrado(userEmail);
                                    } else {
                                        // Verificar en la colección "Usuario"
                                        db.collection("Usuario")
                                                .whereEqualTo("usuario", userEmail)
                                                .get()
                                                .addOnCompleteListener(taskUsuario -> {
                                                    if (taskUsuario.isSuccessful() && taskUsuario.getResult() != null && !taskUsuario.getResult().isEmpty()) {
                                                        // Usuario encontrado en "Usuario"
                                                        redirigirUsuarioRegistrado(userEmail);
                                                    } else {
                                                        // Usuario no registrado en ninguna colección
                                                        redirigirNuevoUsuario(user);
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.w("Firestore", "Error al verificar en 'Usuario'", e));
                                    }
                                })
                                .addOnFailureListener(e -> Log.w("Firestore", "Error al verificar en 'Dueno'", e));
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error al verificar en 'Coach'", e));
    }

    private void redirigirUsuarioRegistrado(String userEmail) {
        Log.d("Firestore", "El usuario ya está registrado: " + userEmail);
        Toast.makeText(Registro.this, "Esta cuenta ya está registrada.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Registro.this, SeleccionUsuario.class);
        startActivity(intent);
        finish();
    }

    private void redirigirNuevoUsuario(FirebaseUser user) {
        Log.d("Firestore", "El usuario no está registrado en ninguna colección, creando nuevo registro.");
        Intent intent = new Intent(Registro.this, SeleccionUsuario.class);
        intent.putExtra("USER_ID", user.getUid()); // Envía el ID del usuario
        intent.putExtra("USER", user);
        startActivity(intent);
        finish();
    }

    private void agregarUsuarioAFirestore(FirebaseUser user) {
        String userId = user.getUid();

        // Crear un mapa para almacenar la información del usuario
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("usuario", user.getEmail());
        usuarioMap.put("nombre", user.getDisplayName());
        usuarioMap.put("telefono", user.getPhoneNumber());
        usuarioMap.put("foto", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        // Guardar el usuario en Firestore
        db.collection("Coach").document(userId)
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario registrado con éxito en Firestore"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error al registrar usuario en Firestore", e));

        Log.d("Firestore", "Usuario UID: " + user.getUid());
        Log.d("Firestore", "Email: " + user.getEmail());
        Log.d("Firestore", "DisplayName: " + user.getDisplayName());
        Log.d("Firestore", "Phone: " + user.getPhoneNumber());
        Log.d("Firestore", "Photo URL: " + (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "No photo"));

    }

    private void verificarCorreoEnFirestore(String email, Runnable onNotRegistered, Runnable onRegistered) {
        // Verificar en la colección "Coach"
        db.collection("Coach")
                .whereEqualTo("usuario", email)
                .get()
                .addOnCompleteListener(taskCoach -> {
                    if (taskCoach.isSuccessful() && taskCoach.getResult() != null && !taskCoach.getResult().isEmpty()) {
                        onRegistered.run();
                    } else {
                        // Verificar en la colección "Dueno"
                        db.collection("Dueno")
                                .whereEqualTo("usuario", email)
                                .get()
                                .addOnCompleteListener(taskDueno -> {
                                    if (taskDueno.isSuccessful() && taskDueno.getResult() != null && !taskDueno.getResult().isEmpty()) {
                                        onRegistered.run();
                                    } else {
                                        // Verificar en la colección "Usuario"
                                        db.collection("Usuario")
                                                .whereEqualTo("usuario", email)
                                                .get()
                                                .addOnCompleteListener(taskUsuario -> {
                                                    if (taskUsuario.isSuccessful() && taskUsuario.getResult() != null && !taskUsuario.getResult().isEmpty()) {
                                                        onRegistered.run();
                                                    } else {
                                                        // No está registrado en ninguna colección
                                                        onNotRegistered.run();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

}
