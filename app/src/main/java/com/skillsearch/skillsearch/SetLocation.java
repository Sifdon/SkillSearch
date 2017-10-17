package com.skillsearch.skillsearch;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Profile;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

//Osmdroid convert: Removed OnMapReadyCallback from implements
public class SetLocation extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    //private GoogleMap mMap;
    public static final String TAG = MainActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    Profile profile;
    GeoFire geoFire;

    //Marker marker;
    double userLat;
    double userLng;
    int iconHeight = 100;
    int iconWidth = 100;
    String uid;

    private MapView map;
    private IMapController mapController;
    ArrayList<OverlayItem> list;
    ItemizedIconOverlay<OverlayItem> iconOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_set_location);

        if (getIntent().getStringExtra(MainActivity.CHANGE_LOCATION) != null) {
            (findViewById(R.id.set_location_progress)).setVisibility(View.GONE);
        }

        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setMultiTouchControls(true);

        list = new ArrayList<>();
        iconOverlay = new ItemizedIconOverlay<>(SetLocation.this, list, null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Location services connected.");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            // Blank for a moment...
        } else {
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            GeoPoint mapFocus = new GeoPoint(currentLatitude, currentLongitude);
            mapController.setCenter(mapFocus);
        }

        DatabaseReference iconReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("profile").child("icon");
        iconReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long iconLong = dataSnapshot.getValue(long.class);
                int icon = Integer.valueOf(String.valueOf(iconLong));
//                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(icon);
//                Bitmap b=bitmapdraw.getBitmap();
//                Bitmap smallMarker = Bitmap.createScaledBitmap(b, iconWidth, iconHeight, false);
//                Drawable iconMarker = new BitmapDrawable(getResources(), smallMarker);
                final Drawable marker = new ScaleDrawable(getResources().getDrawable(icon), 0, iconWidth, iconHeight).getDrawable();

                Overlay overlay = new Overlay() {
                    @Override
                    public void draw(Canvas c, MapView osmv, boolean shadow) {}

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                        //Clear all
                        map.getOverlays().remove(iconOverlay);
                        iconOverlay.removeAllItems();
                        list.clear();

                        //Get location from touch
                        org.osmdroid.views.Projection proj = mapView.getProjection();
                        GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                        userLat = loc.getLatitude();
                        userLng = loc.getLongitude();

                        //Add location to map
                        OverlayItem item = new OverlayItem("", "", new GeoPoint(userLat, userLng));
                        item.setMarker(marker);
                        list.add(item);
                        iconOverlay = new ItemizedIconOverlay<>(SetLocation.this, list, null);
                        map.getOverlays().add(iconOverlay);

                        //Set confirm location to clickable
                        TextView finish = (TextView) findViewById(R.id.user_location_confirm);
                        finish.setBackground(getResources().getDrawable(R.drawable.box_accent));
                        finish.setTextColor(getResources().getColor(R.color.white));
                        finish.setClickable(true);

                        //EXAMPLE FROM NET
//                        if (items == null) {
//                            items = new ItemizedIconOverlay<>(getActivity(), markers, null);
//                            mMapView.getOverlays().add(items);
//                            mMapView.invalidate();
//                        } else {
//                            mMapView.getOverlays().remove(items);
//                            mMapView.invalidate();
//                            items = new ItemizedIconOverlay<>(getActivity(), markers, null);
//                            mMapView.getOverlays().add(items);
//                        }
                        return true;
                    }
                };

                //Google Maps
//                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng point) {
//                        if (marker != null) {
//                            marker.remove();
//                        }
//                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(icon);
//                        Bitmap b=bitmapdraw.getBitmap();
//                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, iconWidth, iconHeight, false);
//
//                        marker = mMap.addMarker(new MarkerOptions()
//                                .position(point)
//                                .anchor(0.5f, 0.5f)
//                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//                        userLocation = point.toString();
//                        userLat = point.latitude;
//                        userLng = point.longitude;
//
//                        TextView finish = (TextView) findViewById(R.id.user_location_confirm);
//                        finish.setBackground(getResources().getDrawable(R.drawable.box_accent));
//                        finish.setTextColor(getResources().getColor(R.color.white));
//                        finish.setClickable(true);
//                    }
//                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void completeProfile(View view) {
        //Set to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("userLocations");
        geoFire = new GeoFire(ref);
        geoFire.setLocation(uid, new GeoLocation(userLat, userLng));

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.getUiSettings().setMapToolbarEnabled(false);
//    }
}
