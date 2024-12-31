package com.kekas.fitverse;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/* loaded from: classes3.dex */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_LATITUDE = "com.example.t3.LATITUDE";
    public static final String EXTRA_LOCATION_NAME = "EXTRA_LOCATION_NAME";
    public static final String EXTRA_LONGITUDE = "com.example.t3.LONGITUDE";
    private GoogleMap mMap;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override // com.google.android.gms.maps.OnMapReadyCallback
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng initialLocation = new LatLng(19.196492d, -99.517503d);
        this.mMap.addMarker(new MarkerOptions().position(initialLocation).title("Ubicación Inicial"));
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12.0f));
        this.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() { // from class: com.example.t3.MapActivity$$ExternalSyntheticLambda0
            @Override // com.google.android.gms.maps.GoogleMap.OnMapClickListener
            public final void onMapClick(LatLng latLng) {
                MapActivity.this.m117lambda$onMapReady$0$comexamplet3MapActivity(latLng);
            }
        });
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