package com.skillsearch.skillsearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
//import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserMessage extends AppCompatActivity {

    public final static String USER_ID_PROFILE = "com.example.cold.skillsearch.USER_ID_PROFILE";
    String userID;
    String chatId;
    Profile profile;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_user_message);

        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Move to bottom of chat
        ScrollView scrollView = (ScrollView)findViewById(R.id.chat_scroll);
        scrollView.fullScroll(View.FOCUS_DOWN);

        //Get user id from intents
        userID = getIntent().getStringExtra(UserProfile.PROFILE_USER_ID);
        if (userID == null) {
            userID = getIntent().getStringExtra(Contacts.CONTACTS_USER_ID);
        }
        if (userID == null) {
            userID = getIntent().getStringExtra(MyFirebaseMessagingService.NOTIF_USER_ID);
        }
        if (userID == null) {
            userID = getIntent().getStringExtra(MainActivity.USER_ID);
        }

        //Get chatId
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("contacts").hasChild(userID)) {
                    chatId = dataSnapshot.child("contacts").child(userID).child("chatId")
                            .getValue(String.class);
                    updateChat();
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //Get chat id from intents
//        chatId = getIntent().getStringExtra(UserProfile.PROFILE_CHAT_ID);
//        if (chatId == null) {
//            chatId = getIntent().getStringExtra(Contacts.CONTACTS_CHAT_ID);
//        }
//        if (chatId == null) {
//            chatId = getIntent().getStringExtra(MyFirebaseMessagingService.NOTIF_CHAT_ID);
//        }

        removeUnreadStatus();

        //Setup contact's info at top
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userID).child("profile");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String facebookId = dataSnapshot.child("facebookId").getValue(String.class);
                long iconLong = dataSnapshot.child("icon").getValue(long.class);
                int iconInt = Integer.valueOf(String.valueOf(iconLong));
                String nameString = dataSnapshot.child("name").getValue(String.class);
                String skillString = dataSnapshot.child("skill").getValue(String.class);

                ProfilePictureView pictureView = (ProfilePictureView)findViewById(R.id.profilePicture);
                TextView nameView = (TextView)findViewById(R.id.mFirstName);
                TextView skillView = (TextView)findViewById(R.id.mSkillPrimary);
                ImageView iconView = (ImageView)findViewById(R.id.category_icon);

                pictureView.setProfileId(facebookId);
                nameView.setText(nameString);
                skillView.setText(skillString);
                iconView.setImageResource(iconInt);
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void removeUnreadStatus() {
        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("contacts");
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userID)) {
                    //If unread message, remove from contact
                    if (dataSnapshot.child(userID).hasChild("unread")) {
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                                .child("contacts").child(userID).child("unread").removeValue();

                        //Remove from unread list
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                                .child("unread").child(userID).removeValue();
                    }
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void updateChat() {
        //Add messages to list view
        if (chatId != null) {
            DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference()
                    .child("userChat")
                    .child(chatId);
            chatReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    append_chat_conversation(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    append_chat_conversation(dataSnapshot);
                }

                @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    public void append_chat_conversation(DataSnapshot dataSnapshot) {
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.message_list);

        for (DataSnapshot child: dataSnapshot.getChildren()) {
            TextView msg = new TextView(this);
            String message = child.getValue(String.class);
            msg.setText(message);
            msg.setTextSize(16);
            msg.setTextColor(getResources().getColor(R.color.black));
            msg.setPadding(15,10,15,10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (child.getKey().equals(userID)) {
                params.gravity = Gravity.START;
                params.setMargins(40,0,100,20);
                msg.setBackground(getResources().getDrawable(R.drawable.box_grey));
            } else {
                params.gravity = Gravity.END;
                params.setMargins(100,0,40,20);
                msg.setBackground(getResources().getDrawable(R.drawable.box_light));
            }
            msg.setLayoutParams(params);
            linearLayout.addView(msg);
        }

        //Move to bottom of chat
        ScrollView scrollView = (ScrollView)findViewById(R.id.chat_scroll);
        scrollView.fullScroll(View.FOCUS_DOWN);

        removeUnreadStatus();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.messageText);
        String message = editText.getText().toString();
        editText.setText("");

        //If chatId exists, add message to chatId, else create chatId and add uid to each other's contacts
        if (chatId != null) {
            DatabaseReference messageId = FirebaseDatabase.getInstance().getReference()
                    .child("userChat")
                    .child(chatId).push();
            DatabaseReference senderId = messageId.child(profile.getId());
            senderId.setValue(message);
        } else {
            //Create chat key
            DatabaseReference userChat = FirebaseDatabase.getInstance().getReference()
                    .child("userChat").push();
            userChat.setValue(userID);
            chatId = userChat.getKey();

            //Add userId, facebookId, user name and skill, and chat id to contacts
            DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userID);
            contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //FacebookId
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .child("contacts").child(userID).child("facebookId")
                            .setValue(dataSnapshot.child("profile").child("facebookId").getValue(String.class));

                    //Name and Skill
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .child("contacts").child(userID).child("nameAndSkill")
                            .setValue(dataSnapshot.child("profile").child("name").getValue(String.class) + ", " +
                                    dataSnapshot.child("profile").child("skill").getValue(String.class));

                    //ChatId
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                            .child("contacts").child(userID).child("chatId").setValue(chatId);
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });

            //Add self, self fbId, self skill, and chat id to user contacts
            DatabaseReference selfRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid);
            selfRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //FacebookId
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                            .child("contacts").child(uid).child("facebookId")
                            .setValue(dataSnapshot.child("profile").child("facebookId").getValue(String.class));

                    //Name and Skill
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                            .child("contacts").child(uid).child("nameAndSkill")
                            .setValue(dataSnapshot.child("profile").child("name").getValue(String.class) + ", " +
                                    dataSnapshot.child("profile").child("skill").getValue(String.class));

                    //ChatId
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                            .child("contacts").child(uid).child("chatId").setValue(chatId);
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });
            //Check for now existing chatId
            updateChat();

            //Add message to chat id   UNCOMMENT ONCE TESTED
            DatabaseReference messageId = FirebaseDatabase.getInstance().getReference()
                    .child("userChat")
                    .child(chatId).push();
            DatabaseReference senderId = messageId.child(uid);
            senderId.setValue(message);
        }

        //Add most recent message and mark as unread
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("contacts")
                .child(userID).child("lastMessage").setValue(message);
        DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(userID).child("contacts").child(uid);
        msgRef.child("lastMessage").setValue(message);
        msgRef.child("unread").setValue(true);
        FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("unread")
                .child(uid).setValue(true);

        //Remove notificationRequest for contact if exists
        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID)
                .child("contacts").child(uid);
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("notification")) {
                    FirebaseDatabase.getInstance().getReference().child("notificationRequests")
                            .child(dataSnapshot.child("notification").getValue(String.class)).removeValue();
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        //COMMENTED OUT FOR TESTING
        //Send message as notification
        /**
        DatabaseReference notification = FirebaseDatabase.getInstance().getReference()
                .child("notificationRequests").push();
        notification.child(userID).setValue(uid);  //Change order after test

        //Set reference to notification key to erase in message received
        FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("contacts").child(uid)     //Change order after test
                .child("notification").setValue(notification.getKey());                                                         //Change order after test (Child is topic, value is sender's userId)
         **/

        //Hide keyboard after message sent
        hideSoftKeyboard(this, view);
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, Contacts.class);
        startActivity(intent);
    }

    public void toUserProfile(View view) {
        Intent intent = new Intent(this, UserProfile.class);
        intent.putExtra(USER_ID_PROFILE, userID);
        startActivity(intent);
        //get other user's id

    }
}
