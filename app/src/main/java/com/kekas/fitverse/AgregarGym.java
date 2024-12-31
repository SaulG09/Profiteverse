package com.kekas.fitverse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgregarGym extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private LatLng selectedLocation;
    private String locationName;
    private LatLng latLng;

    private FirebaseFirestore db; // Añadir Firestore
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private String fotoUrl; // Variable para almacenar la URL de la foto
    EditText nombre;
    private EditText descripcion;
    MaterialCardView enfoque;
    ConstraintLayout cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_gym);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        MaterialCardView foto = findViewById(R.id.cambiarFotoGym);
        nombre = findViewById(R.id.textoNombreGym);
        descripcion = findViewById(R.id.textMapaGym);
        enfoque = findViewById(R.id.botonMapaGym);
        enfoque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgregarGym.this.openMap();
            }
        });
        cr = findViewById(R.id.fotoGymAgregar);
        MaterialButton agregar = findViewById(R.id.botonAgregarGym);

        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener datos de los EditText
                String nuevoNombre = nombre.getText().toString().trim();

                // Validar que latLng no sea null
                if (latLng != null) {
                    Map<String, Object> entrenamiento = new HashMap<>();
                    entrenamiento.put("id_dueno", currentUser.getUid());
                    entrenamiento.put("nombre", nuevoNombre);
                    entrenamiento.put("latitud", latLng.latitude);
                    entrenamiento.put("longitud", latLng.longitude);
                    entrenamiento.put("foto", fotoUrl);


                    // Verificar que el nombre no esté vacío
                    if (!nuevoNombre.isEmpty() && fotoUrl != null) {
                        agregar.setEnabled(false);
                        db.collection("Dueno").document(currentUser.getUid()).collection("Gyms")
                                .add(entrenamiento)
                                .addOnSuccessListener(documentReference -> {
                                    Intent intent = new Intent(AgregarGym.this, Dueno.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> e.printStackTrace());
                    } else {
                        Toast.makeText(AgregarGym.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Si latLng es null, mostrar un mensaje de error
                    Toast.makeText(AgregarGym.this, "Por favor, selecciona una ubicación válida", Toast.LENGTH_SHORT).show();
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
            if (requestCode == 1 && resultCode == -1) {
                double latitude = data.getDoubleExtra(MapActivity.EXTRA_LATITUDE, 0.0d);
                double longitude = data.getDoubleExtra(MapActivity.EXTRA_LONGITUDE, 0.0d);
                this.latLng = new LatLng(latitude, longitude);
                this.selectedLocation = latLng;
                String locationName = getLocationName(latLng.latitude, this.selectedLocation.longitude);
                this.locationName = locationName;
                this.descripcion.setText(locationName);
                Toast.makeText(this, "Ubicación seleccionada: " + this.locationName, Toast.LENGTH_SHORT).show();

        }
    }

    private void actualizarFoto(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        cr.setBackground(drawable);

        subirFoto(bitmap, new OnUploadCompleteListener() {
            @Override
            public void onUploadComplete(String url) {
                Toast.makeText(AgregarGym.this, "Foto subida: " + url, Toast.LENGTH_SHORT).show();
                fotoUrl = url;
            }

            @Override
            public void onUploadFailed(Exception e) {
                Toast.makeText(AgregarGym.this, "Error al subir la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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



    public void openMap() {
        Intent intent = new Intent(this, (Class<?>) MapActivity.class);
        startActivityForResult(intent, 1);
    }
    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return "Ubicación desconocida";
            }
            Address addressData = addresses.get(0);
            String address = addressData.getAddressLine(0);
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return "Ubicación desconocida";
        }
    }
}
