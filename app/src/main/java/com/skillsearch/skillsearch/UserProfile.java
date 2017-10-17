package com.skillsearch.skillsearch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    public final static String PROFILE_USER_ID = "com.skillsearch.skillsearch.PROFILE_USER_ID";
    public final static String PROFILE_CHAT_ID = "com.skillsearch.skillsearch.PROFILE_CHAT_ID";
    String userId;
    Profile profile;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_user_profile);

        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getIntent().getStringExtra(MainActivity.USER_ID) != null) {
            userId = getIntent().getStringExtra(MainActivity.USER_ID);
            (findViewById(R.id.fab)).setVisibility(View.VISIBLE);
        } else if (getIntent().getStringExtra(UserMessage.USER_ID_PROFILE) != null) {
            userId = getIntent().getStringExtra(UserMessage.USER_ID_PROFILE);
            (findViewById(R.id.fab)).setVisibility(View.VISIBLE);
        } else {
            userId = uid;
        }

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long iconLong = dataSnapshot.child("profile").child("icon").getValue(long.class);
                int iconInt = Integer.valueOf(String.valueOf(iconLong));
                String fbIdString = dataSnapshot.child("profile").child("facebookId").getValue(String.class);
                String nameString = dataSnapshot.child("profile").child("name").getValue(String.class);
                String skillString = dataSnapshot.child("profile").child("skill").getValue(String.class);
                String background = dataSnapshot.child("profile").child("background").getValue(String.class);

                ProfilePictureView pictureView = (ProfilePictureView)findViewById(R.id.profilePicture);
                TextView nameView = (TextView)findViewById(R.id.mFirstName);
                TextView skillView = (TextView)findViewById(R.id.mSkillPrimary);
                ImageView iconView = (ImageView)findViewById(R.id.category_icon);
                TextView backgroundView = (TextView)findViewById(R.id.userBackground);

                pictureView.setProfileId(fbIdString);
                nameView.setText(nameString);
                skillView.setText(skillString);
                iconView.setImageResource(iconInt);
                backgroundView.setText(background);

                //Check for other skills
                if (dataSnapshot.child("profile").hasChild("otherSkills")) {
                    String otherSkillString = "";
                    for (DataSnapshot otherSkill: dataSnapshot.child("profile").child("otherSkills").getChildren()) {
                        otherSkillString += otherSkill.getKey() + "\n";
                    }
                    ((TextView)findViewById(R.id.userOtherSkills)).setText(otherSkillString);
                } else {
                    findViewById(R.id.otherSkillsTitle).setVisibility(View.GONE);
                }

                //Check for recommendations
                recRefresh();

                //Display recommendation button if not own profile and uid doesn't exist
                final TextView recButton = (TextView)findViewById(R.id.recommend_button);
                if (!uid.equals(userId) && !dataSnapshot.child("recommendations").hasChild(uid)) {
                    recButton.setText(getResources().getString(R.string.recommend) + " " + nameString + " "
                            + getResources().getString(R.string.to_other_users));
                    recButton.setVisibility(View.VISIBLE);
                    recButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                            LayoutInflater inflater = UserProfile.this.getLayoutInflater();
                            final View recLayout = inflater.inflate(R.layout.recommend_user_dialog, null);
                            builder.setView(recLayout);
                            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String recText = ((EditText) recLayout.findViewById(R.id.rec_skill_text)).getText().toString();
                                    Toast.makeText(UserProfile.this, recText, Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("recommendations")
                                            .child(uid).setValue(recText);
                                    recButton.setVisibility(View.GONE);
                                    dialogInterface.dismiss();
                                    recRefresh();
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
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //FIGURE OUT WHY CONTACT NEEDS TO BE ADDED HERE
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(uid).child("contacts");
                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Intent intent = new Intent(UserProfile.this, UserMessage.class);
                        intent.putExtra(PROFILE_USER_ID, userId);
//                        if (!dataSnapshot.hasChild(userId)) {
//                            intent.putExtra(PROFILE_CHAT_ID, dataSnapshot.child(userId)
//                                    .child("chatId").getValue(String.class));
//                        }
                        startActivity(intent);


//                            //Create chat key
//                            DatabaseReference userChat = FirebaseDatabase.getInstance().getReference()
//                                    .child("userChat").push();
//                            userChat.setValue(userId);
//                            final String chatId = userChat.getKey();
//
//                            //Add user, user skill, and chat id to contacts
//                            DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference()
//                                    .child("users").child(userId);
//                            contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    FirebaseDatabase.getInstance().getReference().child("users").child(profile.getId())
//                                            .child("contacts").child(userId).child("chatId").setValue(chatId);
//
//                                    FirebaseDatabase.getInstance().getReference().child("users").child(profile.getId())
//                                            .child("contacts").child(userId).child("nameAndSkill")
//                                            .setValue(dataSnapshot.child("name").getValue(String.class) + ", " +
//                                                    dataSnapshot.child("skill").getValue(String.class));
//                                }
//                                @Override public void onCancelled(DatabaseError databaseError) {}
//                            });
//
//                            //Add self, self skill, and chat id to user contacts
//                            DatabaseReference selfRef = FirebaseDatabase.getInstance().getReference()
//                                    .child("users").child(profile.getId());
//                            selfRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    FirebaseDatabase.getInstance().getReference().child("users").child(userId)
//                                            .child("contacts").child(profile.getId()).child("chatId").setValue(chatId);
//
//                                    FirebaseDatabase.getInstance().getReference().child("users").child(userId)
//                                            .child("contacts").child(profile.getId()).child("nameAndSkill")
//                                            .setValue(dataSnapshot.child("name").getValue(String.class) + ", " +
//                                                    dataSnapshot.child("skill").getValue(String.class));
//                                }
//                                @Override public void onCancelled(DatabaseError databaseError) {}
//                            });
//                            Intent intent = new Intent(UserProfile.this, UserMessage.class);
//                            intent.putExtra(PROFILE_USER_ID, userId);
//                            intent.putExtra(PROFILE_CHAT_ID, chatId);
//                            startActivity(intent);
//                        } else {
//                            Intent intent = new Intent(UserProfile.this, UserMessage.class);
//                            intent.putExtra(PROFILE_USER_ID, userId);
//                            intent.putExtra(PROFILE_CHAT_ID, dataSnapshot.child(userId).child("chatId").getValue(String.class));
//                            startActivity(intent);
//                        }
                    }
                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });

//        if (userId.equals(profile.getId())) {
//            ((ViewGroup) fab.getParent()).removeView(fab);
//        }
    }

    //Check for recommendations
    public void recRefresh() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("recommendations")) {
                    TextView recommendationsTitle = (TextView) findViewById(R.id.recommendations);
                    String recNumber = getResources().getString(R.string.recommendations) + "   " + String.valueOf(dataSnapshot.child("recommendations").getChildrenCount());
                    recommendationsTitle.setVisibility(View.VISIBLE);
                    recommendationsTitle.setText(recNumber);

                    //Display recommendation comments
                    TextView recommendations = (TextView) findViewById(R.id.userRecommendations);
                    String comments = "";
                    for (DataSnapshot child : dataSnapshot.child("recommendations").getChildren()) {
                        if (!child.getValue(String.class).equals("empty")) {
                            comments += "\n" + child.getValue(String.class) + "\n";
                        }
                    }
                    recommendations.setText(comments);
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
