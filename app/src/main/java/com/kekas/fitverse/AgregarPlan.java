package com.kekas.fitverse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AgregarPlan extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String fotoUrl; // Variable para almacenar la URL de la foto
    EditText nombre;
    EditText descripcion;
    EditText enfoque;
    EditText duracion;
    ConstraintLayout cr;
    private String idRutina;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_plan);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        nombre = findViewById(R.id.textoNombrePlan);
        descripcion = findViewById(R.id.textoDescripcionPlan);
        enfoque = findViewById(R.id.textoPrecioPlan);
        duracion = findViewById(R.id.textoDuracionPlan);

        MaterialButton agregar = findViewById(R.id.botonAgregarPlan);

        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                String nuevoNombre = nombre.getText().toString().trim();
                String nuevoDescripcion = descripcion.getText().toString().trim();
                String enfoqueString = enfoque.getText().toString().trim();
                String duracionString = duracion.getText().toString().trim();

                // Validar que ninguno de los campos esté vacío
                if (nuevoNombre.isEmpty() || nuevoDescripcion.isEmpty() || enfoqueString.isEmpty() || duracionString.isEmpty()) {
                    // Si algún campo está vacío, mostrar un mensaje de error
                    Toast.makeText(AgregarPlan.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;  // Detener la ejecución del código si algún campo está vacío
                }

                // Convertir los valores de enfoque y duración a los tipos correctos
                float nuevaEnfoque = Float.parseFloat(enfoqueString);  // Se usa Float.parseFloat ya que enfoque es un float
                int nuevaDuracion = Integer.parseInt(duracionString);

                // Crear el mapa con los datos
                Map<String, Object> entrenamiento = new HashMap<>();
                entrenamiento.put("id_gym", idRutina);
                entrenamiento.put("nombre", nuevoNombre);
                entrenamiento.put("descripcion", nuevoDescripcion);
                entrenamiento.put("precio", nuevaEnfoque);
                entrenamiento.put("duracion", nuevaDuracion);
                agregar.setEnabled(false);
                // Agregar los datos a la base de datos
                db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina).collection("Planes")
                        .add(entrenamiento)
                        .addOnSuccessListener(documentReference -> {
                            // Si la operación es exitosa, redirigir al usuario
                            //Intent intent = new Intent(AgregarPlan.this, Dueno.class);
                            //startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Manejar el error si la operación falla
                            e.printStackTrace();
                        });
            }
        });


    }
}
