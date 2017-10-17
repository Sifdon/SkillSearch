package com.skillsearch.skillsearch;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.List;

/**
 * Created by COld on 4/10/2016.
 */

public class ContactsAdapter extends ArrayAdapter<Contact> {

    int resource;
    String response;
    Context context;

    public ContactsAdapter(Context context, int resource, List<Contact> items) {
        super(context, resource, items);
        this.resource=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout contactsView;
        Contact contact = getItem(position);
        if(convertView==null) {
            contactsView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, contactsView, true);
        } else {
            contactsView = (LinearLayout) convertView;
        }
        ProfilePictureView profilePicView = (ProfilePictureView)contactsView.findViewById(R.id.userProfilePicture);
        TextView nameAndSkill = (TextView)contactsView.findViewById(R.id.name_and_skill);
        TextView lastMessage = (TextView)contactsView.findViewById(R.id.last_message);
        TextView idCarrier = (TextView)contactsView.findViewById(R.id.id_carrier);
        profilePicView.setProfileId(contact.getFacebookId());
        idCarrier.setText(contact.getUserId());
        nameAndSkill.setText(contact.getNameAndSkill());
        lastMessage.setText(contact.getLastMessage());
        if (contact.getUnread().equals("unread")) {
            lastMessage.setTextColor(Color.parseColor("#000000"));
        }
        return contactsView;
    }
}
