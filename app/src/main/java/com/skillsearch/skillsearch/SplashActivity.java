package com.skillsearch.skillsearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by COld on 4/11/2016.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "FacebookLogin";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        profile = Profile.getCurrentProfile();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("users");
                    profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Go to SelectSkill if no user profile
                            if (dataSnapshot.hasChild(user.getUid())) {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(SplashActivity.this, SelectSkill.class);
                                startActivity(intent);
                            }
                        }
                        @Override public void onCancelled(DatabaseError databaseError) {}
                    });
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //Check if user has profile
//                    DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("users");
//                    profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.hasChild(uid)) {
//                                DatabaseReference locRef = FirebaseDatabase.getInstance().getReference().child("userLocations");
//                                locRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        if (dataSnapshot.hasChild(uid)) {
//                                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                                            startActivity(intent);
//                                        } else {
//                                            //No Location, go to SetLocation
//                                            Intent intent = new Intent(SplashActivity.this, SetLocation.class);
//                                            startActivity(intent);
//                                        }
//                                    }
//                                    @Override public void onCancelled(DatabaseError databaseError) {}
//                                });
//                            } else {
//                                //No user profile, go to SelectSkill
//                                Intent intent = new Intent(SplashActivity.this, SelectSkill.class);
//                                startActivity(intent);
//                            }
//                        }
//                        @Override public void onCancelled(DatabaseError databaseError) {}
//                    });
                } else {
                    //User is not signed in, go to Login
                    Intent intent = new Intent(SplashActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
