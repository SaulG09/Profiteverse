package com.kekas.fitverse;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContrasena extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_contrasena);

        mAuth = FirebaseAuth.getInstance();

        MaterialButton recuperar = findViewById(R.id.recuperarCorreoBoton);
        EditText correo = findViewById(R.id.correoRecuperar);

        recuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = correo.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(RecuperarContrasena.this, "Por favor, introduce tu correo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                recuperar.setEnabled(false);
                // Firebase Auth para enviar correo de restablecimiento
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RecuperarContrasena.this, "Correo enviado. Revisa tu bandeja.", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(RecuperarContrasena.this, "Error al enviar el correo. Inténtalo nuevamente.", Toast.LENGTH_LONG).show();
                                recuperar.setEnabled(true);
                            }
                        });
            }
        });
    }
}
