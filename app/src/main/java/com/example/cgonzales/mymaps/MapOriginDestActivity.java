package com.example.cgonzales.mymaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapOriginDestActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected int PLACE_AUTOCOMPLETE_REQUEST_CODE = 10;

    protected GoogleMap mMap;

    protected double mLatitude;

    protected double mLongitude;

    protected EditText autocompleteView;

    private LatLngBounds bounds;

    private boolean fromAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_chooser);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Maps");
        }

        setupMapFragment();

        autocompleteView = findViewById(R.id.autoCompleteView);
        autocompleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAutoComplete();
            }
        });
    }


    protected void onNewCamera(double latitude, double longitude) {
        autocompleteView.setText(
                LocationUtils.getCompleteAddress(this, latitude, longitude));
    }

    protected void setupMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLng = new LatLng(-12.089336, -77.033845);
        mMap.setLatLngBoundsForCameraTarget(bounds);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (fromAutocomplete) {
                    fromAutocomplete = false;
                } else {
                    final LatLng centerOfMap = mMap.getCameraPosition().target;
                    mLatitude = centerOfMap.latitude;
                    mLongitude = centerOfMap.longitude;
                    //markerOptions.position(centerOfMap);
                    onNewCamera(mLatitude, mLongitude);
                }
            }
        });
    }

    public void launchAutoComplete() {
        try {
            fromAutocomplete = true;
            PlaceAutocomplete.IntentBuilder intentBuilder = new PlaceAutocomplete.IntentBuilder(
                    PlaceAutocomplete.MODE_FULLSCREEN);
            if (bounds != null) {
                intentBuilder.setBoundsBias(bounds);
            }
            startActivityForResult(intentBuilder.build(this), PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e("tag", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onSuccessAutocompletePlace(data);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // Status status = PlaceAutocomplete.getStatus(this, data);
                fromAutocomplete = false;

            } else if (resultCode == RESULT_CANCELED) {
                fromAutocomplete = false;
            } else {
                fromAutocomplete = false;
            }
        }
    }

    private void onSuccessAutocompletePlace(Intent data) {
        Place place = PlaceAutocomplete.getPlace(this, data);
        if (place != null) {
            autocompleteView.setText(place.getName());
            final LatLng latLng = place.getLatLng();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mLatitude = latLng.latitude;
            mLongitude = latLng.longitude;
        }
    }
}
