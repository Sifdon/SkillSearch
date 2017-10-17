package com.skillsearch.skillsearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.R.id.list;
import static com.skillsearch.skillsearch.R.id.map;

//public class MainActivity extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
//        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();
    Profile profile;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static String CHANGE_DETAILS = "com.skillsearch.skillsearch.CHANGE_DETAILS";
    public final static String CHANGE_SKILL = "com.skillsearch.skillsearch.CHANGE_SKILL";
    public final static String USER_ID = "com.skillsearch.skillsearch.USER_ID";
    public final static String ADD_SKILL = "com.skillsearch.skillsearch.ADD_SKILL";
    public final static String CHANGE_LOCATION = "com.skillsearch.skillsearch.CHANGE_LOCATION";
    public final static String SETUP_SKILL = "com.skillsearch.skillsearch.SETUP_SKILL";

    private double currentLatitude;
    private double currentLongitude;

    int iconHeight = 100;
    int iconWidth = 100;
    GeoFire geoFire;
    ProfilePictureView profilePictureView;
    private boolean searchActive;
    private boolean infoWindowOpen;
    private boolean usersNearby;
    private boolean noUserWide;

    String uid;
    private String userIcon;

    private MapView map;
    private IMapController mapController;
    ArrayList<String> userSearchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMaxZoomLevel(17);
        map.setMinZoomLevel(4);
        mapController = map.getController();
        //mapController.setZoom(15);
        map.setMultiTouchControls(true);

        userSearchList = new ArrayList<>();
        searchActive = false;
        infoWindowOpen = false;
        usersNearby = false;
        noUserWide = false;

        profilePictureView = (ProfilePictureView)/*hView.*/findViewById(R.id.profilePicture);
        profilePictureView.setProfileId(profile.getId());
        ((TextView)/*hView.*/findViewById(R.id.mFirstName)).setText(profile.getFirstName());
        DatabaseReference skillRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("profile");
        skillRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("skill") && dataSnapshot.hasChild("icon")) {
                    ((TextView) /*hView.*/findViewById(R.id.mSkillPrimary))
                            .setText(dataSnapshot.child("skill").getValue(String.class));
                    userIcon = String.valueOf(dataSnapshot.child("icon").getValue(long.class));
                    ((ImageView) findViewById(R.id.category_icon)).setImageResource(Integer.parseInt(userIcon));
                } else {
                    TextView setupSkill = (TextView)findViewById(R.id.setup_skill);
                    setupSkill.setVisibility(View.VISIBLE);
                    setupSkill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MainActivity.this, SelectSkill.class);
                            intent.putExtra(SETUP_SKILL, "setup_skill");
                            startActivity(intent);
                        }
                    });
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Contacts.class);
                startActivity(intent);
            }
        });

        //Indicator of unread messages
        final TextView unreadDisplay = (TextView)findViewById(R.id.unread_display);
        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        unreadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("unread")) {
                    unreadDisplay.setVisibility(View.VISIBLE);
                    unreadDisplay.setText(String.valueOf(dataSnapshot.child("unread").getChildrenCount()));
                } else {
                    unreadDisplay.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        ImageView refreshSearch = (ImageView)findViewById(R.id.refresh_search);
        refreshSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLatitude = map.getMapCenter().getLatitude();
                currentLongitude = map.getMapCenter().getLongitude();
                geoFireQuery(100);
            }
        });


        //Check for otherSkills
        removeSkillGone();
    }

    //Check for otherSkills
    public void removeSkillGone () {
        DatabaseReference otherSkillsRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("profile");
        otherSkillsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("otherSkills")) {
                    TextView textView = (TextView)findViewById(R.id.remove_skill);
                    textView.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
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

    public void displayIcon(double latitude, double longitude, final String userId) {
        final GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        //final LatLng latLng = new LatLng(location.latitude, location.longitude);
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("profile");
        userReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        //Setup icon
                        long iconLong = dataSnapshot.child("icon").getValue(long.class);
                        final int iconInt = Integer.valueOf(String.valueOf(iconLong));
                        Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(iconInt)).getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, false);
                        Drawable icon = new BitmapDrawable(getResources(), smallMarker);

                        //Add location to map
                        Marker marker = new Marker(map);
                        marker.setPosition(geoPoint);
                        marker.setIcon(icon);
                        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                marker.closeInfoWindow();
                                mapController.animateTo(marker.getPosition());
                                ProfilePictureView ppv = (ProfilePictureView)findViewById(R.id.info_window_ppv);
                                ppv.setProfileId(dataSnapshot.child("facebookId").getValue(String.class));
                                ImageView catIcon  = (ImageView)findViewById(R.id.info_window_icon);
                                catIcon.setImageResource(iconInt);
                                TextView name = (TextView)findViewById(R.id.info_window_name);
                                name.setText(dataSnapshot.child("name").getValue(String.class));
                                TextView skill = (TextView)findViewById(R.id.info_window_skill);
                                skill.setText(dataSnapshot.child("skill").getValue(String.class));
                                TextView otherSkills = (TextView)findViewById(R.id.info_window_others);
                                String string = "";
                                for (DataSnapshot child: dataSnapshot.child("otherSkills").getChildren()) {
                                    string+=child.getKey()+"\n";
                                }
                                otherSkills.setText(string);

                                //Make info window visible ON BACK PRESSED WHEN OPEN, SET TO GONE
                                infoWindowOpen = true;
                                (findViewById(R.id.info_window)).setVisibility(View.VISIBLE);
                                final TextView viewBlock = (TextView)findViewById(R.id.view_blocker);
                                viewBlock.setVisibility(View.VISIBLE);
                                viewBlock.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        infoWindowOpen = false;
                                        viewBlock.setVisibility(View.GONE);
                                        (findViewById(R.id.info_window)).setVisibility(View.GONE);
                                    }
                                });

                                //Button listeners
                                TextView viewProfile = (TextView)findViewById(R.id.view_profile);
                                viewProfile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(MainActivity.this, UserProfile.class);
                                        intent.putExtra(USER_ID, userId);
                                        startActivity(intent);
                                    }
                                });
                                TextView sendMessage = (TextView)findViewById(R.id.send_message);
                                sendMessage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(MainActivity.this, UserMessage.class);
                                        intent.putExtra(USER_ID, userId);
                                        startActivity(intent);
                                    }
                                });
                                return true;
                            }
                        });
                        map.getOverlays().add(marker);
                        map.invalidate();

//                        OverlayItem item = new OverlayItem(userId, "", new GeoPoint(geoPoint));
//                        item.setMarker(icon);
//                        //Can't get item out of onDataChange? May need to use markers
//                        list.add(item);
                    } @Override public void onCancelled(DatabaseError databaseError) {}
                });
    }

    public void geoFireQuery (int distance) {
        map.getOverlays().clear();
        //Adjust zoom level
//        if (searchActive) {
//            mapController.setZoom(13);
//        } else {
//            mapController.setZoom(15);
//        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("userLocations");
        geoFire = new GeoFire(ref);
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLatitude, currentLongitude), distance);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String userId, GeoLocation location) {
                if (!userId.equals(uid)) {
                    usersNearby = true;
                    if (!searchActive) {
                        //Search for nearby skills
                        displayIcon(location.latitude, location.longitude, userId);
                    } else {
                        //Search for specific skills
                        if (userSearchList.contains(userId)) {
                            displayIcon(location.latitude, location.longitude, userId);
                        }
                    }
                }
            }
            @Override public void onKeyExited(String key) {}
            @Override public void onKeyMoved(String key, GeoLocation location) {}
            @Override public void onGeoQueryReady() {
                //IF NO USERS NEARBY REGARDLESS OF SEARCH TYPE, DO WIDER SEARCH
                
                if (!usersNearby && searchActive) {
                    (Toast.makeText(MainActivity.this, R.string.no_users_nearby, Toast.LENGTH_SHORT)).show();
                } else if (usersNearby && searchActive) {
                    mapController.setZoom(13);
                } else if (!usersNearby && !searchActive) {
                    if (!noUserWide){
                        geoFireQuery(1000);
                        mapController.setZoom(13);
                        noUserWide = true;
                    } else {
                        Toast.makeText(MainActivity.this, R.string.no_users_nearby, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mapController.setZoom(15);
                }
                usersNearby = false;
            }
            @Override public void onGeoQueryError(DatabaseError error) {}
        });
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
        }
        else {
            //Setup Map
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            GeoPoint mapFocus = new GeoPoint(currentLatitude, currentLongitude);
            mapController.setCenter(mapFocus);

            //Geoquery 100km radius
            geoFireQuery(100);
        }

        //Query skills in toolbar
        DatabaseReference skillCatReference = FirebaseDatabase.getInstance().getReference().child("categorisedSkills");
        skillCatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Skill> skillList = new ArrayList<>();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    for (DataSnapshot skill: category.getChildren()) {
                        skillList.add(new Skill(skill.getKey(), category.getKey()));
                    }
                }

                //Setup autocomplete TextView
                SkillAdapter adapter = new SkillAdapter(MainActivity.this, skillList);
                AutoCompleteTextView actv = (AutoCompleteTextView)findViewById(R.id.skill_text_search);
                actv.setThreshold(1);
                actv.setAdapter(adapter);

                //Get users of clicked skill
                actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        searchActive = true;
                        userSearchList.clear();

                        //Hide keyboard after message sent
                        hideSoftKeyboard(MainActivity.this, view);

                        final String skill = ((TextView)view.findViewById(R.id.skill_type)).getText().toString();
                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                                .child("skillList");
                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //final ArrayList<String> userList = new ArrayList<>();
                                if (dataSnapshot.hasChild(skill)) {
                                    for (DataSnapshot child: dataSnapshot.child(skill).getChildren()) {
                                        userSearchList.add(child.getKey());
                                    }
                                    //Geofire query 1000km
                                    currentLatitude = map.getMapCenter().getLatitude();
                                    currentLongitude = map.getMapCenter().getLongitude();
                                    geoFireQuery(1000);
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.no_users_nearby, Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchActive) {
            searchActive = false;
            geoFireQuery(100);
        } else if (infoWindowOpen) {
            infoWindowOpen = false;
            LinearLayout linearLayout = (LinearLayout)findViewById(R.id.info_window);
            linearLayout.setVisibility(View.GONE);
            (findViewById(R.id.view_blocker)).setVisibility(View.GONE);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.exit_app);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory( Intent.CATEGORY_HOME );
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
            });
            builder.show();
        }
    }

    public void viewProfile (View view) {
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
    }

    public void changeSkill (View view) {
        DatabaseReference skillRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("profile").child("skill");
        skillRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(MainActivity.this, SelectSkill.class);
                intent.putExtra(CHANGE_SKILL, dataSnapshot.getValue(String.class));
                startActivity(intent);
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void changeDetails (View view) {
        Intent intent = new Intent(this, SelectSkill.class);
        intent.putExtra(CHANGE_DETAILS, "change_details");
        startActivity(intent);
    }

    public void changeLocation (View view) {
        Intent intent = new Intent(this, SelectSkill.class);
        intent.putExtra(CHANGE_LOCATION, userIcon);
        startActivity(intent);
    }

    public void addSkill (View view) {
        DatabaseReference otherSkills = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("profile");
        otherSkills.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("otherSkills")) {
                    if (dataSnapshot.child("otherSkills").getChildrenCount() < 5) {
                        Intent intent = new Intent(MainActivity.this, SelectSkill.class);
                        intent.putExtra(ADD_SKILL, "add_skill");
                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.other_skill_count, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, SelectSkill.class);
                    intent.putExtra(ADD_SKILL, "add_skill");
                    startActivity(intent);
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void removeSkill (View view) {
        DatabaseReference otherSkillRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("profile").child("otherSkills");
        otherSkillRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    adapter.add(child.getKey());
                }
                AlertDialog.Builder dialogList = new AlertDialog.Builder(MainActivity.this);
                dialogList.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialogList.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String clickedSkill = adapter.getItem(i);
                        final AlertDialog.Builder dialogConfirm = new AlertDialog.Builder(MainActivity.this);
                        dialogConfirm
                                .setMessage("Remove " + clickedSkill + " from other skills?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //TO DO Remove from category skill list too
                                        FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedSkill)
                                                .child(uid).removeValue();
                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("profile")
                                                .child("otherSkills").child(clickedSkill).removeValue();
                                        removeSkillGone();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        dialogConfirm.show();
                    }
                });
                dialogList.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Consider swapping for fb login button
    public void logOut (View view) {
        Intent intent = new Intent(this, Login.class);
        //Facebook logout
        LoginManager.getInstance().logOut();
        //Firebase signout
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}