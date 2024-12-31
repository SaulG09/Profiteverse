package com.kekas.fitverse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AgregarRutina extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_rutina);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        MaterialCardView foto = findViewById(R.id.cambiarFotoRutina);
        nombre = findViewById(R.id.textoNombreRutina);
        descripcion = findViewById(R.id.textoDescripcionRutina);
        enfoque = findViewById(R.id.textoEnfoqueRutina);
        duracion = findViewById(R.id.textoDuracionRutina);
        cr = findViewById(R.id.fotoRutina);
        MaterialButton agregar = findViewById(R.id.botonAgregarRutina);

        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                String nuevoNombre = nombre.getText().toString().trim();
                String nuevoDescripcion = descripcion.getText().toString().trim();
                String nuevaEnfoque = enfoque.getText().toString().trim();
                String nuevaDuracion = duracion.getText().toString().trim();

                // Validar que los campos no estén vacíos y que la foto no sea null
                if (nuevoNombre.isEmpty() || nuevoDescripcion.isEmpty() || nuevaEnfoque.isEmpty() || nuevaDuracion.isEmpty() || fotoUrl == null) {
                    Toast.makeText(AgregarRutina.this, "Por favor, completa todos los campos y agrega una foto", Toast.LENGTH_SHORT).show();
                } else {
                    // Si los campos son válidos, crear el mapa y agregar los datos a Firestore
                    Map<String, Object> entrenamiento = new HashMap<>();
                    entrenamiento.put("id_coach", currentUser.getUid());
                    entrenamiento.put("nombre", nuevoNombre);
                    entrenamiento.put("descripcion", nuevoDescripcion);
                    entrenamiento.put("enfoque", nuevaEnfoque);
                    entrenamiento.put("duracion", nuevaDuracion);
                    entrenamiento.put("foto", fotoUrl);
                    agregar.setEnabled(false);
                    // Agregar los datos a Firestore
                    db.collection("Coach").document(currentUser.getUid()).collection("Rutinas")
                            .add(entrenamiento)
                            .addOnSuccessListener(documentReference -> {
                                Intent intent = new Intent(AgregarRutina.this, Coach.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> e.printStackTrace());
                }
            }
        });


        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirOpcionesFoto();
            }
        });
    }

    private void abrirOpcionesFoto() {
        String[] opciones = {"Tomar foto", "Elegir de galería"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirCamara();
                    } else if (which == 1) {
                        abrirGaleria();
                    }
                })
                .show();
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

    private void abrirGaleria() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null && data.getExtras() != null) {
                Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
                if (photoBitmap != null) {
                    actualizarFoto(photoBitmap);
                }
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        InputStream input = this.getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true; // Solo calcula las dimensiones
                        BitmapFactory.decodeStream(input, null, options);
                        input.close();

                        // Define las dimensiones máximas permitidas
                        int maxWidth = 1024;
                        int maxHeight = 1024;

                        // Calcula el factor de escala
                        int scaleFactor = Math.max(
                                options.outWidth / maxWidth,
                                options.outHeight / maxHeight
                        );

                        // Redimensiona el Bitmap
                        options.inJustDecodeBounds = false; // Carga el Bitmap completo
                        options.inSampleSize = scaleFactor; // Aplica el factor de escala

                        input = this.getContentResolver().openInputStream(imageUri);
                        Bitmap resizedBitmap = BitmapFactory.decodeStream(input, null, options);
                        input.close();

                        // Llama a tu método con el Bitmap redimensionado
                        actualizarFoto(resizedBitmap);
                        /*Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        actualizarFoto(galleryBitmap);*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void actualizarFoto(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        cr.setBackground(drawable);

        subirFoto(bitmap, new OnUploadCompleteListener() {
            @Override
            public void onUploadComplete(String url) {
                Toast.makeText(AgregarRutina.this, "Foto subida: " + url, Toast.LENGTH_SHORT).show();

                fotoUrl = url;
            }

            @Override
            public void onUploadFailed(Exception e) {
                Toast.makeText(AgregarRutina.this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirFoto(Bitmap bitmap, OnUploadCompleteListener listener) {
        String fotoId = "foto_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("fotos/" + fotoId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> listener.onUploadComplete(downloadUri.toString()))
                    .addOnFailureListener(listener::onUploadFailed);
        }).addOnFailureListener(listener::onUploadFailed);
    }

    public interface OnUploadCompleteListener {
        void onUploadComplete(String url);
        void onUploadFailed(Exception e);
    }
}
