package com.skillsearch.skillsearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SelectSkill extends AppCompatActivity implements MapEventsReceiver,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //SET LOCATION
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    GeoFire geoFire;
    double userLat;
    double userLng;
    int iconHeight = 100;
    int iconWidth = 100;
    Marker marker;
    //SET LOCATION

    View categoriesLayout;
    View listLayout;
    View rolesLayout;
    View descriptionLayout;
    View locationLayout;

    ImageView userIcon;

    private MapView map;
    private IMapController mapController;
    ArrayList<OverlayItem> list;
    ItemizedIconOverlay<OverlayItem> iconOverlay;

    Profile profile;
    private int categoryIcon;
    private String clickedSkill;
    private String clickedCategory;
    private String clickedRole;
    private String background;
    private String uid;

    private String skillRemove;
    private String changeLocation;
    private boolean addSkill;
    private boolean changeDetails;
    private boolean setupSkill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_select_skill);
        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Temp code to add all skills
//        DatabaseReference fb = FirebaseDatabase.getInstance().getReference().child("categorisedSkills");
//        fb.child("Mechanic/Car Mechanic").setValue(0);
//        fb.child("Mechanic/Diesel Mechanic").setValue(0);
//        fb.child("Mechanic/Motorcycle Mechanic").setValue(0);
//        fb.child("Mechanic/Bicycle Mechanic").setValue(0);
//
//        fb.child("DIY/Builder").setValue(0);
//        fb.child("DIY/Repair").setValue(0);
//        fb.child("DIY/Painter").setValue(0);
//        fb.child("DIY/Plumber").setValue(0);
//        fb.child("DIY/Electrician").setValue(0);
//        fb.child("DIY/Lawn Mowing").setValue(0);
//
//        fb.child("Beauty/Hairdressing").setValue(0);
//        fb.child("Beauty/Makeup").setValue(0);
//        fb.child("Beauty/Jewellery").setValue(0);
//        fb.child("Beauty/Manicure").setValue(0);
//        fb.child("Beauty/Beauty Products").setValue(0);
//        fb.child("Beauty/Fashion").setValue(0);
//
//        fb.child("Childcare/Babysitter").setValue(0);
//        fb.child("Childcare/Daycare").setValue(0);
//        fb.child("Childcare/After School Care").setValue(0);
//
//        fb.child("IT/Computer Repair").setValue(0);
//        fb.child("IT/Programmer").setValue(0);
//        fb.child("IT/Social Networking").setValue(0);
//        fb.child("IT/Mobile Developer").setValue(0);
//        fb.child("IT/Game Developer").setValue(0);
//
//        fb.child("Artist/Photography").setValue(0);
//        fb.child("Artist/Digital Art").setValue(0);
//        fb.child("Artist/Entertainer").setValue(0);
//        fb.child("Artist/DJ").setValue(0);
//        fb.child("Artist/Musician").setValue(0);
//        fb.child("Artist/Film").setValue(0);
//
//        fb.child("Culinary/Chef").setValue(0);
//        fb.child("Culinary/Butcher").setValue(0);
//        fb.child("Culinary/Baker").setValue(0);
//        fb.child("Culinary/Restaurant").setValue(0);
//        fb.child("Culinary/Produce").setValue(0);
//
//        fb.child("Tutor/High School Tutor").setValue(0);
//        fb.child("Tutor/Language Teacher").setValue(0);
//        fb.child("Tutor/Personal Trainer").setValue(0);
//        fb.child("Tutor/Counselling").setValue(0);
//
//        fb.child("Professional/Legal").setValue(0);
//        fb.child("Professional/Accounting").setValue(0);
//        fb.child("Professional/Business").setValue(0);
//        fb.child("Professional/Public Relations").setValue(0);
//        fb.child("Professional/Management").setValue(0);
//        fb.child("Professional/Marketing").setValue(0);
//        fb.child("Professional/Medical").setValue(0);
//        fb.child("Professional/Office Support").setValue(0);
//
//        fb.child("Transport/Chauffeur").setValue(0);
//        fb.child("Transport/Taxi Service").setValue(0);
//        fb.child("Transport/Delivery Service").setValue(0);
//        fb.child("Transport/Logistics").setValue(0);
//        fb.child("Transport/Mover").setValue(0);
//
//        fb.child("Hospitality/Hotel").setValue(0);
//        fb.child("Hospitality/Motel").setValue(0);
//        fb.child("Hospitality/Spare Room").setValue(0);
//        fb.child("Hospitality/Couch or Mattress").setValue(0);
//        fb.child("Hospitality/B&B").setValue(0);
//        fb.child("Hospitality/Camp Ground").setValue(0);
//
//        fb.child("Other/Labour").setValue(0);
//        fb.child("Other/Gym Buddy").setValue(0);
//        fb.child("Other/Drinking Buddy").setValue(0);

        userIcon = (ImageView)findViewById(R.id.category_icon);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(17);
        map.setMultiTouchControls(true);
        list = new ArrayList<>();
        iconOverlay = new ItemizedIconOverlay<>(this, list, null);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        categoriesLayout = findViewById(R.id.sCategories);
        listLayout = findViewById(R.id.sList);
        rolesLayout = findViewById(R.id.sRole);
        descriptionLayout = findViewById(R.id.sDescription);
        locationLayout = findViewById(R.id.sLocation);

        //View display pic
        ((ProfilePictureView) findViewById(R.id.profilePicture)).setProfileId(
                profile.getId()
        );

        //View name
        ((TextView) findViewById(R.id.mFirstName)).setText(profile.getFirstName()
        );

        addSkill = false;
        changeDetails = false;
        setupSkill = false;

        //Get intent extras
        skillRemove = getIntent().getStringExtra(MainActivity.CHANGE_SKILL);
        if (getIntent().getStringExtra(MainActivity.ADD_SKILL) != null) {
            addSkill = true;
            (findViewById(R.id.category_progress)).setVisibility(View.GONE);
        } else if (getIntent().getStringExtra(MainActivity.CHANGE_DETAILS) != null) {
            changeDetails = true;
            toSkillDescription();
            LinearLayout progress = (LinearLayout)findViewById(R.id.skill_description_progress);
            progress.setVisibility(View.GONE);
            categoriesLayout.setVisibility(View.GONE);
            descriptionLayout.setVisibility(View.VISIBLE);
        } else if (getIntent().getStringExtra(MainActivity.CHANGE_LOCATION) != null) {
            changeLocation = getIntent().getStringExtra(MainActivity.CHANGE_LOCATION);
            categoryIcon = Integer.parseInt(changeLocation);
            categoriesLayout.setVisibility(View.GONE);
            locationLayout.setVisibility(View.VISIBLE);
            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(SelectSkill.this, SelectSkill.this);
            map.getOverlays().add(0, mapEventsOverlay);
            LinearLayout progress = (LinearLayout)findViewById(R.id.set_location_progress);
            progress.setVisibility(View.GONE);
        } else if (getIntent().getStringExtra(MainActivity.SETUP_SKILL) != null) {
            setupSkill = true;
        }

        //Check for existing main skill and Icon
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("profile");
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView mSkill = (TextView) findViewById(R.id.mSkillPrimary);
                ImageView mCategory = (ImageView) findViewById(R.id.category_icon);
                if (dataSnapshot.hasChild("skill")) {
                    String skill = dataSnapshot.child("skill").getValue(String.class);
                    mSkill.setText(skill);
                } else {
                    //Skip to mainActivity option. Display add skill button on Main activity
                    TextView skipSkill = (TextView)findViewById(R.id.skip_skill_select);
                    skipSkill.setVisibility(View.VISIBLE);
                    skipSkill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(uid).child("profile");
                            userRef.child("facebookId").setValue(profile.getId());
                            userRef.child("name").setValue(profile.getFirstName());
                            Intent intent = new Intent(SelectSkill.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                if (dataSnapshot.hasChild("icon")) {
                    long iconLong = dataSnapshot.child("icon").getValue(long.class);
                    int icon = Integer.valueOf(String.valueOf(iconLong));
                    mCategory.setImageResource(icon);
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //Setup skills for actv skill selection
        setupACTV(false);
    }

    public void setupACTV(final boolean specific) {
        DatabaseReference skillCatReference = FirebaseDatabase.getInstance().getReference().child("categorisedSkills");
        skillCatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Skill> skillList = new ArrayList<>();
                if (specific) {
                    skillList.clear();
                    for (DataSnapshot skill : dataSnapshot.child(clickedCategory).getChildren()) {
                        skillList.add(new Skill(skill.getKey(), clickedCategory));
                    }
                } else {
                    skillList.clear();
                    for (DataSnapshot category: dataSnapshot.getChildren()) {
                        for (DataSnapshot skill: category.getChildren()) {
                            skillList.add(new Skill(skill.getKey(), category.getKey()));
                        }
                    }
                }

                //Setup autocomplete TextView
                SkillAdapter adapter = new SkillAdapter(SelectSkill.this, skillList);
                AutoCompleteTextView actv = (AutoCompleteTextView)findViewById(R.id.select_skill_search);
                actv.setThreshold(1);
                actv.setAdapter(adapter);

                //Get users of clicked skill
                actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        clickedSkill = ((TextView)view.findViewById(R.id.skill_type)).getText().toString();
                        clickedCategory = ((TextView)view.findViewById(R.id.category_text)).getText().toString();
                        switch (clickedCategory) {
                            case "Mechanic" : userIcon.setImageResource(R.drawable.mechanic); break;
                            case "DIY" : userIcon.setImageResource(R.drawable.diy); break;
                            case "Beauty" : userIcon.setImageResource(R.drawable.beauty); break;
                            case "Childcare" : userIcon.setImageResource(R.drawable.childcare); break;
                            case "IT" : userIcon.setImageResource(R.drawable.infotech); break;
                            case "Artist" : userIcon.setImageResource(R.drawable.artist); break;
                            case "Culinary" : userIcon.setImageResource(R.drawable.culinary); break;
                            case "Tutor" : userIcon.setImageResource(R.drawable.tutor); break;
                            case "Professional" : userIcon.setImageResource(R.drawable.professional_advice); break;
                            case "Transport" : userIcon.setImageResource(R.drawable.transport); break;
                            case "Hospitality" : userIcon.setImageResource(R.drawable.hospitality); break;
                            case "Other" : userIcon.setImageResource(R.drawable.general); break;
                        }
                        //Hide keyboard after message sent
                        hideSoftKeyboard(SelectSkill.this, view);

                        toSkillRole();
                    }
                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void onBackPressed() {
        if ((findViewById(R.id.tableLayout).getVisibility() == View.VISIBLE && (addSkill || setupSkill || skillRemove != null))
                || changeDetails  || changeLocation != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if ((findViewById(R.id.skill_list_layout).getVisibility() == View.VISIBLE)) {
            findViewById(R.id.skill_list_layout).setVisibility(View.GONE);
            findViewById(R.id.tableLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.recommend_skill).setVisibility(View.GONE);
            setupACTV(false);
        }else if (locationLayout.getVisibility() == View.VISIBLE) {
            locationLayout.setVisibility(View.GONE);
            descriptionLayout.setVisibility(View.VISIBLE);
        } else if (descriptionLayout.getVisibility() == View.VISIBLE) {
            descriptionLayout.setVisibility(View.GONE);
            rolesLayout.setVisibility(View.VISIBLE);
        } else if (rolesLayout.getVisibility() == View.VISIBLE) {
            rolesLayout.setVisibility(View.GONE);
            categoriesLayout.setVisibility(View.VISIBLE);
        } else {
            //Do nothing
        }
    }

    //Remove category layout and replace with list of skills
    public void replaceTable(int icon, final String category) {
//        categoriesLayout.setVisibility(View.GONE);
//        listLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.tableLayout).setVisibility(View.GONE);
        findViewById(R.id.skill_list_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.skip_skill_select).setVisibility(View.GONE);

        //Recommend skill on click
        TextView recommendText = (TextView)findViewById(R.id.recommend_skill);
        recommendText.setVisibility(View.VISIBLE);
        recommendText.setText(getResources().getString(R.string.recommend_to_category) + " " + category + "?");
        recommendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectSkill.this);
                LayoutInflater inflater = SelectSkill.this.getLayoutInflater();
                final View recLayout = inflater.inflate(R.layout.recommend_skill_dialog, null);
                builder.setView(recLayout);
//                final EditText editText = new EditText(SelectSkill.this);
//                editText.setHint(R.string.skill);
//                builder.setView(editText);
                builder.setPositiveButton(R.string.recommend, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String recSkill = ((EditText)recLayout.findViewById(R.id.rec_skill_text)).getText().toString();
                        FirebaseDatabase.getInstance().getReference().child("recommendedSkills").child(recSkill)
                                .child(uid).setValue(category);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        //Set category
        clickedCategory = category;

        //Setup category banner
        ImageView skillIcon = (ImageView) findViewById(R.id.skill_icon);
        skillIcon.setImageResource(icon);
        userIcon.setImageResource(icon);
        TextView skillCategory = (TextView) findViewById(R.id.skill_category);
        skillCategory.setText(category);

        //Update actv
        setupACTV(true);

        //Populate skill grid
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("categorisedSkills")
                .child(category);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> skillArray = new ArrayList<>();
                for (DataSnapshot skill : dataSnapshot.getChildren()) {
                    skillArray.add(skill.getKey());
                }
                Collections.sort(skillArray);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectSkill.this, R.layout.text_center, R.id.textItem, skillArray);
                GridView skillList = (GridView) findViewById(R.id.skill_list);
                skillList.setAdapter(adapter);
                skillList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        TextView textItem = (TextView) view.findViewById(R.id.textItem);
                        clickedSkill = textItem.getText().toString();
                        toSkillRole();
                    }
                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //On category button pressed
    public void openMechanic(View view) {
        replaceTable(R.drawable.mechanic, getResources().getString(R.string.mechanic));}
    public void openBeauty(View view) {
        replaceTable(R.drawable.beauty, getResources().getString(R.string.beauty));}
    public void openDIY(View view) {
        replaceTable(R.drawable.diy, getResources().getString(R.string.diy));}
    public void openChildcare(View view) {
        replaceTable(R.drawable.childcare, getResources().getString(R.string.childcare));}
    public void openArtist(View view) {
        replaceTable(R.drawable.artist, getResources().getString(R.string.artist));}
    public void openInfotech(View view) {
        replaceTable(R.drawable.infotech, getResources().getString(R.string.infotech));}
    public void openTutor(View view) {
        replaceTable(R.drawable.tutor, getResources().getString(R.string.tutor));}
    public void openCulinary(View view) {
        replaceTable(R.drawable.culinary, getResources().getString(R.string.culinary));}
    public void openProfessionalAdvice(View view) {
        replaceTable(R.drawable.professional_advice, getResources().getString(R.string.professional));}
    public void openTransport(View view) {
        replaceTable(R.drawable.transport, getResources().getString(R.string.transport));}
    public void openHospitality(View view) {
        replaceTable(R.drawable.hospitality, getResources().getString(R.string.hospitality));}
    public void openGeneral(View view) {
        replaceTable(R.drawable.general, getResources().getString(R.string.general));}

    public void toSkillRole() {
        //Adding additional skill to database
        if (addSkill) {
            //Add to profile
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("profile")
                    .child("otherSkills").child(clickedSkill).setValue("placeholder");
            //Add to skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedSkill)
                    .child(uid).setValue("secondary");
            //Add to category skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedCategory)
                    .child(uid).setValue("secondary");

            //Return to main
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            //Remove layout and replace with skill roles layout
            categoriesLayout.setVisibility(View.GONE);
            rolesLayout.setVisibility(View.VISIBLE);

            //Update banner with skill
            TextView mSkill = (TextView) findViewById(R.id.mSkillPrimary);
            mSkill.setText(clickedSkill);
        }
    }

    //Method to resize role buttons and set category icon
    public void resizeButtons (int select, int unselect1, int unselect2, int unselect3) {
        LinearLayout.LayoutParams selected = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams unselected = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        selected.setMargins(0,0,0,0);
        unselected.setMargins(8,8,8,8);

        TextView helper = (TextView)findViewById(select);
        helper.setPadding(12,12,12,12);
        helper.setLayoutParams(selected);
        TextView business = (TextView)findViewById(unselect1);
        business.setPadding(8,8,8,8);
        business.setLayoutParams(unselected);
        TextView learner = (TextView)findViewById(unselect2);
        learner.setPadding(8,8,8,8);
        learner.setLayoutParams(unselected);
        TextView teacher = (TextView)findViewById(unselect3);
        teacher.setPadding(8,8,8,8);
        teacher.setLayoutParams(unselected);
    }

    public void setCategoryIcon(int icon) {
        categoryIcon = icon;
        userIcon.setImageResource(icon);
    }

    //Skill Role buttons
    public void helperClick(View view) {
        clickedRole = getResources().getString(R.string.helper);

        //Increase button size and shrink others if selected
        resizeButtons(R.id.helper_button, R.id.business_button, R.id.learner_button, R.id.teacher_button);

        //Show role description
        TextView roleDescription = (TextView)findViewById(R.id.role_description);
        roleDescription.setText(R.string.helper_description);

        //Make 'next' button clickable
        TextView confirmRole = (TextView)findViewById(R.id.role_confirm);
        confirmRole.setTextColor(getResources().getColor(R.color.white));
        confirmRole.setBackground(getResources().getDrawable(R.drawable.box_accent));
        confirmRole.setClickable(true);

        switch (clickedCategory) {
            case "Mechanic" : setCategoryIcon(R.drawable.mechanic); break;
            case "DIY" : setCategoryIcon(R.drawable.diy); break;
            case "Beauty" : setCategoryIcon(R.drawable.beauty); break;
            case "Childcare" : setCategoryIcon(R.drawable.childcare); break;
            case "IT" : setCategoryIcon(R.drawable.infotech); break;
            case "Artist" : setCategoryIcon(R.drawable.artist); break;
            case "Culinary" : setCategoryIcon(R.drawable.culinary); break;
            case "Tutor" : setCategoryIcon(R.drawable.tutor); break;
            case "Professional" : setCategoryIcon(R.drawable.professional_advice); break;
            case "Transport" : setCategoryIcon(R.drawable.transport); break;
            case "Hospitality" : setCategoryIcon(R.drawable.hospitality); break;
            case "Other" : setCategoryIcon(R.drawable.general); break;
        }
    }
    public void learnerClick(View view) {
        clickedRole = getResources().getString(R.string.learner);

        //Increase button size and shrink others if selected
        resizeButtons(R.id.learner_button, R.id.business_button, R.id.helper_button, R.id.teacher_button);

        //Show role description
        TextView roleDescription = (TextView)findViewById(R.id.role_description);
        roleDescription.setText(R.string.learner_description);

        //Make 'next' button clickable
        TextView confirmRole = (TextView)findViewById(R.id.role_confirm);
        confirmRole.setTextColor(getResources().getColor(R.color.white));
        confirmRole.setBackground(getResources().getDrawable(R.drawable.box_accent));
        confirmRole.setClickable(true);

        //Add Icon to header
        switch (clickedCategory) {
            case "Mechanic" : setCategoryIcon(R.drawable.learner_mechanic); break;
            case "DIY" : setCategoryIcon(R.drawable.learner_diy); break;
            case "Beauty" : setCategoryIcon(R.drawable.learner_beauty); break;
            case "Childcare" : setCategoryIcon(R.drawable.learner_childcare); break;
            case "IT" : setCategoryIcon(R.drawable.learner_infotech); break;
            case "Artist" : setCategoryIcon(R.drawable.learner_artist); break;
            case "Culinary" : setCategoryIcon(R.drawable.learner_culinary); break;
            case "Tutor" : setCategoryIcon(R.drawable.learner_tutor); break;
            case "Professional" : setCategoryIcon(R.drawable.learner_professional_advice); break;
            case "Transport" : setCategoryIcon(R.drawable.learner_transport); break;
            case "Hospitality" : setCategoryIcon(R.drawable.learner_hospitality); break;
            case "Other" : setCategoryIcon(R.drawable.learner_general); break;
        }
    }
    public void teacherClick(View view) {
        clickedRole = getResources().getString(R.string.teacher);

        //Increase button size and shrink others if selected
        resizeButtons(R.id.teacher_button, R.id.business_button, R.id.helper_button, R.id.learner_button);

        //Show role description
        TextView roleDescription = (TextView)findViewById(R.id.role_description);
        roleDescription.setText(R.string.teacher_description);

        //Make 'next' button clickable
        TextView confirmRole = (TextView)findViewById(R.id.role_confirm);
        confirmRole.setTextColor(getResources().getColor(R.color.white));
        confirmRole.setBackground(getResources().getDrawable(R.drawable.box_accent));
        confirmRole.setClickable(true);

        //Add Icon to header
        switch (clickedCategory) {
            case "Mechanic" : setCategoryIcon(R.drawable.teacher_mechanic); break;
            case "DIY" : setCategoryIcon(R.drawable.teacher_diy); break;
            case "Beauty" : setCategoryIcon(R.drawable.teacher_beauty); break;
            case "Childcare" : setCategoryIcon(R.drawable.teacher_childcare); break;
            case "IT" : setCategoryIcon(R.drawable.teacher_infotech); break;
            case "Artist" : setCategoryIcon(R.drawable.teacher_artist); break;
            case "Culinary" : setCategoryIcon(R.drawable.teacher_culinary); break;
            case "Tutor" : setCategoryIcon(R.drawable.teacher_tutor); break;
            case "Professional" : setCategoryIcon(R.drawable.teacher_professional_advice); break;
            case "Transport" : setCategoryIcon(R.drawable.teacher_transport); break;
            case "Hospitality" : setCategoryIcon(R.drawable.teacher_hospitality); break;
            case "Other" : setCategoryIcon(R.drawable.teacher_general); break;
        }
    }
    public void businessClick(View view) {
        clickedRole = getResources().getString(R.string.business);

        //Increase button size and shrink others if selected
        resizeButtons(R.id.business_button, R.id.learner_button, R.id.helper_button, R.id.teacher_button);

        //Show role description
        TextView roleDescription = (TextView)findViewById(R.id.role_description);
        roleDescription.setText(R.string.business_description);
    }

    public void toBackground(View view) {
        toSkillDescription();
    }

    public void toSkillDescription() {
        //Remove layout and replace with skill description layout
        rolesLayout.setVisibility(View.GONE);
        descriptionLayout.setVisibility(View.VISIBLE);

        //Get background if exists
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("profile");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("background")) {
                    EditText background = (EditText)findViewById(R.id.edit_background);
                    background.setText(dataSnapshot.child("background").getValue(String.class));
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void toMap(View view) {
        EditText backgroundText = (EditText) findViewById(R.id.edit_background);
        background = backgroundText.getText().toString();

        hideSoftKeyboard(this, view);

        //Remove previous skill from skill list (Changing main skill)
        if (skillRemove != null) {
            //Remove from skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(skillRemove)
                    .child(uid).removeValue();

            //Remove from category skill list
            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(uid).child("profile").child("category");
            categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child("skillList")
                            .child(dataSnapshot.getValue(String.class)).child(uid).removeValue();
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });

            //Save Profile
            DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(uid).child("profile");
            profileRef.child("skill").setValue(clickedSkill);
            profileRef.child("category").setValue(clickedCategory);
            profileRef.child("role").setValue(clickedRole);
            profileRef.child("icon").setValue(categoryIcon);
            profileRef.child("background").setValue(background);

            //Add to skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedSkill)
                    .child(uid).setValue("main");

            //Add to category skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedCategory)
                    .child(uid).setValue("main");

            //Return to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (changeDetails) {
            //Update background and return to MainActivity
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("profile")
                    .child("background").setValue(background);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
                //Setup location layout and map to receive touch events
                descriptionLayout.setVisibility(View.GONE);
                locationLayout.setVisibility(View.VISIBLE);
                MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(SelectSkill.this, SelectSkill.this);
                map.getOverlays().add(0, mapEventsOverlay);
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        //Remove marker and reset touch overlay
        map.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(SelectSkill.this, SelectSkill.this);
        map.getOverlays().add(0, mapEventsOverlay);

        //Location values for database
        userLat = geoPoint.getLatitude();
        userLng = geoPoint.getLongitude();

        //Setup user icon
        Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(categoryIcon)).getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, false);
        Drawable icon = new BitmapDrawable(getResources(), smallMarker);

        //Setup marker, disable info window
        Marker marker = new Marker(map);
        marker.setIcon(icon);
        marker.setPosition(geoPoint);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.closeInfoWindow();
                return true;
            }
        });
        map.getOverlays().add(marker);
        map.invalidate();

        //Set confirm location to clickable
        TextView finish = (TextView) findViewById(R.id.user_location_confirm);
        finish.setBackground(getResources().getDrawable(R.drawable.box_accent));
        finish.setTextColor(getResources().getColor(R.color.white));
        finish.setClickable(true);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    public void completeProfile(View view) {
        //Replace button with progress bar
        (findViewById(R.id.user_location_confirm)).setVisibility(View.GONE);
        (findViewById(R.id.progress_bar_loc)).setVisibility(View.VISIBLE);
        if (changeLocation != null) {
            //Save new location
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("userLocations");
            geoFire = new GeoFire(ref);
            geoFire.setLocation(uid, new GeoLocation(userLat, userLng));
        } else {
            //Save Location
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("userLocations");
            geoFire = new GeoFire(ref);
            geoFire.setLocation(uid, new GeoLocation(userLat, userLng));

            //Save Profile
            DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(uid).child("profile");
            profileRef.child("facebookId").setValue(profile.getId());
            profileRef.child("name").setValue(profile.getFirstName());
            profileRef.child("skill").setValue(clickedSkill);
            profileRef.child("category").setValue(clickedCategory);
            profileRef.child("role").setValue(clickedRole);
            profileRef.child("icon").setValue(categoryIcon);
            profileRef.child("background").setValue(background);

            //Add to skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedSkill)
                    .child(uid).setValue("main");
            //Add to category skill list
            FirebaseDatabase.getInstance().getReference().child("skillList").child(clickedCategory)
                    .child(uid).setValue("main");

            //Setup to receive notifications
            FirebaseMessaging.getInstance().subscribeToTopic(uid);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //SET LOCATION
    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));}
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();}
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();}
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Location services connected.");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
