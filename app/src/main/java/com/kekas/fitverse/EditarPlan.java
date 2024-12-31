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
import com.google.android.gms.maps.model.LatLng;
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

public class EditarPlan extends AppCompatActivity {
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
    private String idPlan;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_plan);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        idPlan = intent.getStringExtra("ID_PLAN");

        nombre = findViewById(R.id.textoNombrePlanE);
        descripcion = findViewById(R.id.textoDescripcionPlanE);
        enfoque = findViewById(R.id.textoPrecioPlanE);
        duracion = findViewById(R.id.textoDuracionPlanE);
        llenarInfo();

        MaterialButton agregar = findViewById(R.id.botonAgregarPlanE);

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
                    Toast.makeText(EditarPlan.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;  // Detener la ejecución del código si algún campo está vacío
                }

                // Intentar convertir los valores de enfoque y duración a los tipos correctos
                float nuevaEnfoque;
                int nuevaDuracion;
                try {
                    nuevaEnfoque = Float.parseFloat(enfoqueString);  // Convertir enfoque a float
                } catch (NumberFormatException e) {
                    // Si enfoque no es un número válido, mostrar un mensaje de error
                    Toast.makeText(EditarPlan.this, "El enfoque debe ser un número válido", Toast.LENGTH_SHORT).show();
                    return;  // Detener la ejecución del código si enfoque es inválido
                }

                try {
                    nuevaDuracion = Integer.parseInt(duracionString);  // Convertir duracion a int
                } catch (NumberFormatException e) {
                    // Si duración no es un número válido, mostrar un mensaje de error
                    Toast.makeText(EditarPlan.this, "La duración debe ser un número válido", Toast.LENGTH_SHORT).show();
                    return;  // Detener la ejecución del código si duracion es inválido
                }

                // Crear el mapa con los datos
                Map<String, Object> entrenamiento = new HashMap<>();
                entrenamiento.put("id_gym", idRutina);
                entrenamiento.put("nombre", nuevoNombre);
                entrenamiento.put("descripcion", nuevoDescripcion);
                entrenamiento.put("precio", nuevaEnfoque);
                entrenamiento.put("duracion", nuevaDuracion);

                // Actualizar los datos en la base de datos
                db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina).collection("Planes").document(idPlan)
                        .update(entrenamiento)
                        .addOnSuccessListener(documentReference -> {
                            // Si la operación es exitosa, finalizar la actividad
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Manejar el error si la operación falla
                            e.printStackTrace();
                        });
            }
        });
    }


    private void llenarInfo(){
        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina).collection("Planes").document(idPlan)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        nombre.setText(document.getString("nombre"));
                        descripcion.setText(document.getString("descripcion"));
                        enfoque.setText(document.getLong("precio").toString());

                        duracion.setText(document.getLong("duracion").toString());

                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

}
