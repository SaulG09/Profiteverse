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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class DatosUsuario extends AppCompatActivity {

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String fotoUrl; // Variable para almacenar la URL de la foto
    EditText nombre;
    EditText telefono;

    ConstraintLayout cr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_datos_usuario);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String idUser = intent.getStringExtra("USER_ID");

        MaterialCardView foto = findViewById(R.id.cambiarFotoDueno4);
        nombre = findViewById(R.id.textoNombreDueno4);
        telefono = findViewById(R.id.textoTelefonoDueno4);
        cr = findViewById(R.id.fotoDueno4);
        MaterialButton finalizar = findViewById(R.id.finalizarDueno4);

        finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                String nuevoNombre = nombre.getText().toString().trim();
                String nuevoTelefono = telefono.getText().toString().trim();

                // Validar que los campos no estén vacíos
                if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
                    // Mostrar un mensaje si algún campo está vacío
                    Toast.makeText(DatosUsuario.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Crear un mapa de datos para actualizar
                    Map<String, Object> datosActualizados = new HashMap<>();
                    datosActualizados.put("nombre", nuevoNombre);
                    datosActualizados.put("telefono", nuevoTelefono);

                    // Comprobar si la foto se ha subido
                    if (fotoUrl != null) {
                        datosActualizados.put("foto", fotoUrl);


                        // Actualizar en Firestore
                        db.collection("Usuario").document(idUser)
                                .update(datosActualizados)
                                .addOnSuccessListener(aVoid -> Toast.makeText(DatosUsuario.this, "Datos actualizados", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(DatosUsuario.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show());

                        // Redirigir a la actividad Usuario después de la actualización
                        Intent intent = new Intent(DatosUsuario.this, Usuario.class);
                        intent.putExtra("ID_USER", idUser);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(DatosUsuario.this, "Sube una foto primero.", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara();
            }
        });

        // Verificar si se recibió el ID del usuario
        if (idUser != null) {
            obtenerNombreUsuario(idUser, nombre, cr, telefono);
        }
    }

    private void abrirCamara() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            }
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara necesario para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
                if (photoBitmap != null) {
                    Drawable drawable = new BitmapDrawable(getResources(), photoBitmap);
                    ConstraintLayout cr = findViewById(R.id.fotoDueno4);
                    cr.setBackground(drawable);

                    // Al tomar la foto, llama a subirFoto y maneja el resultado
                    subirFoto(photoBitmap, new OnUploadCompleteListener() {
                        @Override
                        public void onUploadComplete(String url) {
                            // Aquí puedes usar la URL de descarga
                            Toast.makeText(DatosUsuario.this, "Foto subida: " + url, Toast.LENGTH_SHORT).show();
                            fotoUrl = url; // Almacenar la URL en la variable de instancia
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            // Maneja el error
                            Toast.makeText(DatosUsuario.this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void subirFoto(Bitmap bitmap, OnUploadCompleteListener listener) {
        // Generar un ID único para la foto
        String fotoId = "foto_" + System.currentTimeMillis() + ".jpg";

        // Referencia al almacenamiento
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("fotos/" + fotoId);

        // Convertir el Bitmap a un ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Subir el archivo
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Obtener la URL de descarga
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                listener.onUploadComplete(downloadUri.toString());
            }).addOnFailureListener(listener::onUploadFailed);
        }).addOnFailureListener(listener::onUploadFailed);
    }

    // Interfaz para el callback
    public interface OnUploadCompleteListener {
        void onUploadComplete(String url);
        void onUploadFailed(Exception e);
    }

    private void obtenerNombreUsuario(String userId, EditText nombreEditText, ConstraintLayout cr, EditText tele) {
        db.collection("Usuario").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String foto = document.getString("foto");
                            String telef = document.getString("telefono");

                            if (nombre != null) {
                                nombreEditText.setText(nombre);
                            }
                            if (telef != null) {
                                tele.setText(telef);
                            }


                            if (foto != null) {
                                Glide.with(this)
                                        .load(foto)
                                        .into(new CustomTarget<Drawable>() {
                                            @Override
                                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                cr.setBackground(resource);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}
