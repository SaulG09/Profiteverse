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

public class AfiliarCoach extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String fotoUrl; // Variable para almacenar la URL de la foto
    EditText busqueda;
    TextView nombre;
    TextView entrada;
    TextView salida;
    ConstraintLayout fotoCoach;
    MaterialCardView relojEntrada;
    MaterialCardView relojSalida;
    MaterialButton afiliar;
    private String idRutina;
    private String idCoach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_afiliar_coach);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        idRutina = intent.getStringExtra("ID_GYM");
        busqueda = findViewById(R.id.buscarCoach);
        nombre = findViewById(R.id.nombreCoachAfiliar);
        entrada = findViewById(R.id.entradaCoachAfiliado);
        salida = findViewById(R.id.salidaCoachAfiliado);
        relojEntrada = findViewById(R.id.relojEntrada);
        relojSalida = findViewById(R.id.relojSalida);
        fotoCoach = findViewById(R.id.fotoCoachAfiliar);
        afiliar = findViewById(R.id.afiliarCoach);

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


        busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Acción antes de que cambie el texto (opcional)
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Acción mientras el texto cambia
                // Por ejemplo, podrías actualizar la vista en tiempo real
                actualizar(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Acción después de que el texto cambie
                // Puedes acceder al texto final con s.toString()
            }
        });
            afiliar.setEnabled(false);
        afiliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                Map<String, Object> entrenamiento = new HashMap<>();
                entrenamiento.put("id_gym", idRutina);
                entrenamiento.put("entrada", entrada.getText().toString());
                entrenamiento.put("salida", salida.getText().toString());
                entrenamiento.put("id_coach", idCoach);

                // Verificar que los campos de entrada y salida no estén vacíos
                if (!salida.getText().toString().isEmpty() && !entrada.getText().toString().isEmpty()) {
                    // Consulta para verificar si ya existe el id_coach con el id_gym
                    db.collection("Cargo")
                            .whereEqualTo("id_coach", idCoach)
                            .whereEqualTo("id_gym", idRutina)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult().isEmpty()) {
                                    // Si no existe una combinación duplicada, proceder a agregar el nuevo documento
                                    afiliar.setEnabled(false);
                                    db.collection("Cargo")
                                            .add(entrenamiento)
                                            .addOnSuccessListener(documentReference -> {
                                                //Intent intent = new Intent(AfiliarCoach.this, Dueno.class);
                                                //startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> e.printStackTrace());
                                } else if (task.isSuccessful()) {
                                    // Si ya existe la combinación, muestra un mensaje al usuario
                                    Toast.makeText(AfiliarCoach.this, "Este coach ya está afiliado a este gimnasio.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Si ocurre un error en la consulta
                                    task.getException().printStackTrace();
                                    Toast.makeText(AfiliarCoach.this, "Error al verificar la afiliación.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Si los campos de entrada o salida están vacíos
                    Toast.makeText(AfiliarCoach.this, "Por favor, ingresa la hora de entrada y salida.", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void actualizar(CharSequence seq) {
        db.collection("Coach").whereEqualTo("usuario", seq.toString())
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

}
