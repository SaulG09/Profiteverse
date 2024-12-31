package com.kekas.fitverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/* loaded from: classes3.dex */
public class MapaGyms extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_LATITUDE = "com.example.t3.LATITUDE";
    public static final String EXTRA_LOCATION_NAME = "EXTRA_LOCATION_NAME";
    public static final String EXTRA_LONGITUDE = "com.example.t3.LONGITUDE";
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    DocumentSnapshot document2;
    private FirebaseFirestore db;


    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_gyms);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng initialLocation = new LatLng(19.196492, -99.517503);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 9.0f));

        // Consulta Firestore para obtener todos los gimnasios y añadir sus marcadores
        db.collectionGroup("Gyms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Double latitude = document.getDouble("latitud");
                            Double longitude = document.getDouble("longitud");
                            String gymName = document.getString("nombre");
                            String gymId = document.getId(); // Extrae el ID del gimnasio

                            if (latitude != null && longitude != null && gymId != null) {
                                LatLng gymLocation = new LatLng(latitude, longitude);

                                // Agrega una marca en el mapa para el gimnasio con el nombre como título
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(gymLocation)
                                        .title(gymName != null ? gymName : "Gimnasio")
                                        .snippet("Haz clic para más detalles"));

                                // Asigna el ID del gimnasio y otros datos al marcador
                                if (marker != null) {
                                    Map<String, Object> tagData = new HashMap<>();
                                    tagData.put("id_gym", gymId);
                                    tagData.put("id_dueno", document.getString("id_dueno"));
                                    marker.setTag(tagData);
                                    marker.showInfoWindow(); // Muestra la ventana de información automáticamente
                                }
                            }
                        }

                        // Configura el evento de clic en las marcas
                        mMap.setOnMarkerClickListener(marker -> {
                            Map<String, Object> tagData = (Map<String, Object>) marker.getTag();
                            if (tagData != null) {
                                String gymId = (String) tagData.get("id_gym");
                                String idDueno = (String) tagData.get("id_dueno");

                                if (gymId != null && idDueno != null) {
                                    // Crea el Intent para pasar los datos
                                    Intent intent = new Intent(MapaGyms.this, InformacionGimnasio.class);
                                    intent.putExtra("ID_GYM", gymId); // Envía el ID del gimnasio
                                    intent.putExtra("ID_DUENO", idDueno); // Envía el ID del dueño
                                    startActivity(intent);
                                }
                            }
                            return true; // Previene que se cierre la ventana de información al hacer clic
                        });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }





    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onMapReady$0$com-example-t3-MapActivity, reason: not valid java name */
    public /* synthetic */ void m117lambda$onMapReady$0$comexamplet3MapActivity(LatLng latLng) {
        this.mMap.clear();
        //this.mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LATITUDE, latLng.latitude);
        resultIntent.putExtra(EXTRA_LONGITUDE, latLng.longitude);
        resultIntent.putExtra(EXTRA_LOCATION_NAME, "Nombre de la ubicación");
        setResult(-1, resultIntent);
        finish();
    }
}