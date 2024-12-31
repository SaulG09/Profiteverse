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
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditarCargo extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    EditText busqueda;
    TextView nombre;
    TextView entrada;
    TextView salida;
    ConstraintLayout fotoCoach;
    MaterialCardView relojEntrada;
    MaterialCardView relojSalida;
    MaterialButton afiliar;
    private String idRutina;
    private String fotoUrl;

    private String idCoach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_cargo);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_CARGO");
        nombre = findViewById(R.id.nombreCoachAfiliar2);
        entrada = findViewById(R.id.entradaCoachAfiliado2);
        salida = findViewById(R.id.salidaCoachAfiliado2);
        relojEntrada = findViewById(R.id.relojEntrada2);
        relojSalida = findViewById(R.id.relojSalida2);
        fotoCoach = findViewById(R.id.fotoCoachAfiliar2);
        afiliar = findViewById(R.id.afiliarCoach2);
        relojSalida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtén la hora actual como valores predeterminados
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Crea y muestra el TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                // Formatea la hora seleccionada en 00:00 y actualiza el TextView
                                String horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute);
                                salida.setText(horaFormateada);
                            }
                        }, hour, minute, true); // Usa true para formato de 24 horas, false para 12 horas
                timePickerDialog.show();
            }
        });

        relojEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtén la hora actual como valores predeterminados
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Crea y muestra el TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                // Formatea la hora seleccionada en 00:00 y actualiza el TextView
                                String horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute);
                                entrada.setText(horaFormateada);
                            }
                        }, hour, minute, true); // Usa true para formato de 24 horas, false para 12 horas
                timePickerDialog.show();
            }
        });

        afiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                Map<String, Object> entrenamiento = new HashMap<>();
                entrenamiento.put("entrada", entrada.getText().toString());
                entrenamiento.put("salida", salida.getText().toString());

                // Verificar que los campos de entrada y salida no estén vacíos
                if (!salida.getText().toString().isEmpty() && !entrada.getText().toString().isEmpty()) {
                    // Consulta para verificar si ya existe el id_coach con el id_gym
                    db.collection("Cargo").document(idRutina)
                            .update(entrenamiento)
                            .addOnSuccessListener(documentReference -> {
                                //Intent intent = new Intent(EditarCargo.this, Dueno.class);
                                //startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> e.printStackTrace());

                } else {
                    // Si los campos de entrada o salida están vacíos
                    Toast.makeText(EditarCargo.this, "Por favor, ingresa la hora de entrada y salida.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        actualizar();


    }

    private void actualizar() {

        db.collection("Cargo").document(idRutina)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentCargo = task.getResult();
                        idCoach = documentCargo.getString("id_coach");
                        db.collection("Coach").document(idCoach)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot documentCoach = task2.getResult();
                                        nombre.setText(documentCoach.getString("nombre"));
                                        salida.setText(documentCargo.getString("salida"));
                                        entrada.setText(documentCargo.getString("entrada"));
                                        fotoUrl = documentCoach.getString("foto");
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
                                    } else {

                                    }
                                })
                                .addOnFailureListener(task2 -> {

                                });
                    } else {

                    }
                })
                .addOnFailureListener(task -> {
                    // Acción si ocurrió un error en la búsqueda

                });



    }

}
