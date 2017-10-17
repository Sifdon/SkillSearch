package com.skillsearch.skillsearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

public class Contacts extends AppCompatActivity {

    //private ArrayAdapter<String> arrayAdapter;
    //private ArrayList<String> list_of_contacts = new ArrayList<>();
    private ListView listView;
    private EditText editText;
    public final static String CONTACTS_CHAT_ID = "com.example.cold.skillsearch.CONTACTS_CHAT_ID";
    public final static String CONTACTS_USER_ID = "com.example.cold.skillsearch.CONTACTS_USER_ID";

    Profile profile;
    String uid;
    String query;

    ArrayList<Contact> list_of_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_contacts);

        profile = Profile.getCurrentProfile();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.contacts_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list_of_contacts = new ArrayList<>();
        listView = (ListView)findViewById(R.id.contact_list);

        displayContacts();

        editText = (EditText)findViewById(R.id.contact_text_search);
        editText.addTextChangedListener(contactSearch);
    }

    private final TextWatcher contactSearch = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            query = editText.getText().toString();
            (Toast.makeText(Contacts.this, query, Toast.LENGTH_SHORT)).show();
            list_of_contacts.clear();
            displayContacts();
        }

        @Override public void afterTextChanged(Editable editable) {}
    };

    public void displayContacts() {
        DatabaseReference contactRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("contacts");
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot contact: dataSnapshot.getChildren()) {
                    String userId = contact.getKey();
                    String facebookId = contact.child("facebookId").getValue(String.class);
                    String nameAndSkill = contact.child("nameAndSkill").getValue(String.class);
                    String lastMessage;
                    if (contact.hasChild("lastMessage")) {
                        lastMessage = contact.child("lastMessage").getValue(String.class);
                    } else {
                        lastMessage = "";
                    }

                    //if query in place
                    if (query != null) {
                        if (nameAndSkill.toLowerCase().contains(query.toLowerCase())) {
                            if (contact.hasChild("unread")) {
                                list_of_contacts.add(new Contact(userId, facebookId, nameAndSkill, lastMessage, "unread"));
                            } else {
                                list_of_contacts.add(new Contact(userId, facebookId, nameAndSkill, lastMessage, ""));
                            }
                        }
                        //Else include all contacts
                    } else {
                        if (contact.hasChild("unread")) {
                            list_of_contacts.add(new Contact(userId, facebookId, nameAndSkill, lastMessage, "unread"));
                        } else {
                            list_of_contacts.add(new Contact(userId, facebookId, nameAndSkill, lastMessage, ""));
                        }
                    }
                }
                ContactsAdapter adapter = new ContactsAdapter(Contacts.this, R.layout.contact_list_item, list_of_contacts);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String userId = ((TextView)view.findViewById(R.id.id_carrier)).getText().toString();
                        DatabaseReference contactIdRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(uid).child("contacts").child(userId);
                        contactIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Intent intent = new Intent(Contacts.this, UserMessage.class);
                                //Toast.makeText(getApplicationContext(), dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
                                intent.putExtra(CONTACTS_USER_ID, dataSnapshot.getKey());
                                //intent.putExtra(CONTACTS_CHAT_ID, dataSnapshot.child("chatId").getValue(String.class));
                                startActivity(intent);
                            }
                            @Override public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
