package com.kekas.fitverse;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditarSuscripcion extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;

    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String fotoUrl; // Variable para almacenar la URL de la foto

    TextView nombre;
    ConstraintLayout fotoCoach;
    Spinner planes;
    MaterialButton afiliar;
    private String idRutina;
    private String idGym;
    private String idCoach;      FirebaseUser currentUser;
    private String idPlan;
    long duracion;
    ArrayList<Map<String, String>> listaPlanes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_suscripcion);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_SUS");
        idGym = intent.getStringExtra("ID_GYM");

        nombre = findViewById(R.id.nombreUsuarioAfiliar2);
        planes = findViewById(R.id.spinnerPlan2);
        cargarPlanes();




        fotoCoach = findViewById(R.id.fotoUsuarioAfiliar2);
        afiliar = findViewById(R.id.afiliarUsuario2);

        afiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                Map<String, Object> entrenamiento = new HashMap<>();

                for (Map<String, String> plan : listaPlanes) {
                    if (plan.get("nombre").equals(planes.getSelectedItem().toString())) {
                        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idGym)
                                .collection("Planes").document(plan.get("id"))
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        entrenamiento.put("id_plan", document.getId());

                                        // Asegúrate de que duracion es un Long
                                        Long duracionLong = document.getLong("duracion");

                                        // Calcular la fecha de término solo después de obtener la duración
                                        Calendar calendar = Calendar.getInstance();
                                        String inicio = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                                                (calendar.get(Calendar.MONTH) + 1) + "/" +
                                                calendar.get(Calendar.YEAR);
                                        entrenamiento.put("inicio", inicio);

                                        // Verifica si duracionLong no es null y luego convierte a int
                                        if (duracionLong != null) {
                                            // Convertir el Long a int antes de sumarlo
                                            calendar.add(Calendar.DAY_OF_MONTH, duracionLong.intValue());
                                        } else {
                                            // Maneja el caso en que la duración no esté presente o sea nula
                                            return; // Sale de la ejecución si no hay duración
                                        }

                                        // Calcular la fecha de término
                                        String termino = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                                                (calendar.get(Calendar.MONTH) + 1) + "/" +
                                                calendar.get(Calendar.YEAR);
                                        entrenamiento.put("termino", termino);
                                        entrenamiento.put("estado", "Activo");

                                        // Mostrar la fecha de término

                                        // Verificar si ya existe una suscripción
                                        db.collection("Suscripcion").document(idRutina)
                                                .update(entrenamiento)
                                                .addOnSuccessListener(documentReference -> {
                                                    //Intent intent = new Intent(EditarCargo.this, Dueno.class);
                                                    //startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> e.printStackTrace());
                                    } else {
                                        Toast.makeText(EditarSuscripcion.this, "Error al cargar los datos del plan.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                    }
                }
            }
        });
    cargarInfo();



    }

    private void actualizar(CharSequence seq) {
        db.collection("Usuario").whereEqualTo("usuario", seq.toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot documentCoach : task.getResult()) {
                            nombre.setText(documentCoach.getString("nombre"));
                            String fotoUrl = documentCoach.getString("foto");
                            idCoach = documentCoach.getId();
                            if (fotoUrl != null) {
                                Glide.with(this)
                                        .load(fotoUrl)
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                fotoCoach.setBackground(resource);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }
                                        });
                            }
                            afiliar.setEnabled(true);
                        }
                    } else {
                        // Acción si la búsqueda no fue exitosa o no hay resultados
                        afiliar.setEnabled(false);
                        nombre.setText("");
                        fotoCoach.setBackground(null);
                        idCoach = "";
                    }
                })
                .addOnFailureListener(task -> {
                    // Acción si ocurrió un error en la búsqueda
                    afiliar.setEnabled(false);
                    nombre.setText("");
                    fotoCoach.setBackground(null);
                    idCoach = "";
                });
    }


    private void cargarPlanes() {
        listaPlanes = new ArrayList<>();
        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idGym)
                .collection("Planes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> nombresPlanes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombrePlan = document.getString("nombre");
                            String idPlanDoc = document.getId();
                            if (nombrePlan != null) {
                                Map<String, String> plan = new HashMap<>();
                                plan.put("nombre", nombrePlan);
                                plan.put("id", idPlanDoc);
                                listaPlanes.add(plan);
                                nombresPlanes.add(nombrePlan);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresPlanes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        planes.setAdapter(adapter);

                        // Configurar el listener después de cargar los datos
                        planes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String elementoSeleccionado = parent.getItemAtPosition(position).toString();

                                for (Map<String, String> plan : listaPlanes) {
                                    if (plan.get("nombre").equals(elementoSeleccionado)) {
                                        idPlan = plan.get("id");
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Opcional: acción cuando no se selecciona nada
                            }
                        });
                    } else {
                        Toast.makeText(this, "Error al cargar los planes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private long obtenerPlan() {
        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms").document(idRutina)
                .collection("Planes").document(idPlan)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        duracion = document.getLong("duracion");

                    } else {
                        Toast.makeText(this, "Error al cargar los planes.", Toast.LENGTH_SHORT).show();
                    }
                });
        return duracion;
    }

    private void cargarInfo(){
        db.collection("Suscripcion").document(idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String idCoach = document.getString("id_usuario");
                        db.collection("Usuario").document(idCoach)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot documentCoach = task2.getResult();
                                        // Crear y configurar cada card
                                        nombre.setText(documentCoach.getString("nombre"));
                                        //especializacion.setText(obtenerPlan(document.getString("id_plan")));
                                        String fotoUrl = documentCoach.getString("foto");
                                        if (fotoUrl != null) {
                                            Glide.with(this)
                                                    .load(fotoUrl)
                                                    .into(new CustomTarget<Drawable>() {
                                                        @Override
                                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                            fotoCoach.setBackground(resource);
                                                        }

                                                        @Override
                                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                                        }
                                                    });
                                        }


                                    }
                                })
                                .addOnFailureListener(e -> e.printStackTrace());
                    } else {
                        Toast.makeText(this, "Error al cargar los planes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
