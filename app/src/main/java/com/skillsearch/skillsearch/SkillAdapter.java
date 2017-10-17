package com.skillsearch.skillsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by COld on 6/03/2017.
 */

public class SkillAdapter extends ArrayAdapter<Skill> {

//    private Skill skill;
//    private TextView skillText;
//    private ImageView categoryImage;
//    private List<Skill> skillList = new ArrayList<>();
//    private final CategoryIconRetriever categoryIconRetriever;

//    ArrayList<Skill> skills =
//
//    public SkillAdapter(Context context, int textViewResourceId, List<Skill> objects) {
//        super(context, textViewResourceId, objects);
//        skillList = objects;
//    }
//
//    @Override
//    public int getCount() {
//        return this.skillList.size();
//    }
//
//    @Override
//    public Skill getItem(int position) {
//        return this.skillList.get(position);
//    }
//
//    @Override
//    public View getView(int positon, View convertView, ViewGroup parent) {
//        View row = convertView;
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        if (row == null) {
//            row = inflater.inflate(R.layout.skill_item, parent, false);
//        }
//
//        skill = this.skillList.get(positon);
//
//        TextView skillType = (TextView)row.findViewById(R.id.skill_type);
//        ImageView skillCategory = (ImageView) row.findViewById(R.id.skill_cat);
//        skillType.setText(skill.getSkill());
//        switch (skill.getCategory()) {
//            case "Mechanic" : skillCategory.setImageResource(R.drawable.mechanic); break;
//            case "DIY" : skillCategory.setImageResource(R.drawable.diy); break;
//            case "Beauty" : skillCategory.setImageResource(R.drawable.beauty); break;
//            case "Childcare" : skillCategory.setImageResource(R.drawable.childcare); break;
//            case "IT" : skillCategory.setImageResource(R.drawable.infotech); break;
//            case "Artist" : skillCategory.setImageResource(R.drawable.artist); break;
//            case "Culinary" : skillCategory.setImageResource(R.drawable.culinary); break;
//            case "Tutor" : skillCategory.setImageResource(R.drawable.tutor); break;
//            case "Professional Advice" : skillCategory.setImageResource(R.drawable.professional_advice); break;
//            case "Transport" : skillCategory.setImageResource(R.drawable.transport); break;
//            case "Hospitality" : skillCategory.setImageResource(R.drawable.hospitality); break;
//            case "General Help" : skillCategory.setImageResource(R.drawable.general); break;
//        }
//
//        return row;
//    }
    ArrayList<Skill> skills, tempSkill, suggestions ;

    public SkillAdapter(Context context,ArrayList<Skill> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.skills = objects ;
        this.tempSkill =new ArrayList<Skill>(objects);
        this.suggestions =new ArrayList<Skill>(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Skill skill = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.skill_item, parent, false);
        }

        TextView skillText = (TextView) convertView.findViewById(R.id.skill_type);
        ImageView categoryImage = (ImageView) convertView.findViewById(R.id.skill_cat);
        TextView categoryText = (TextView) convertView.findViewById(R.id.category_text);

        if (skillText != null)
            skillText.setText(skill.getSkill());

        if (categoryImage != null) {
                switch (skill.getCategory()) {
                case "Mechanic" : categoryImage.setImageResource(R.drawable.mechanic); break;
                case "DIY" : categoryImage.setImageResource(R.drawable.diy); break;
                case "Beauty" : categoryImage.setImageResource(R.drawable.beauty); break;
                case "Childcare" : categoryImage.setImageResource(R.drawable.childcare); break;
                case "IT" : categoryImage.setImageResource(R.drawable.infotech); break;
                case "Artist" : categoryImage.setImageResource(R.drawable.artist); break;
                case "Culinary" : categoryImage.setImageResource(R.drawable.culinary); break;
                case "Tutor" : categoryImage.setImageResource(R.drawable.tutor); break;
                case "Professional" : categoryImage.setImageResource(R.drawable.professional_advice); break;
                case "Transport" : categoryImage.setImageResource(R.drawable.transport); break;
                case "Hospitality" : categoryImage.setImageResource(R.drawable.hospitality); break;
                case "Other" : categoryImage.setImageResource(R.drawable.general); break;
            }
        }

        if (categoryText != null) {
            categoryText.setText(skill.getCategory());
        }

        // Now assign alternate color for rows
//        if (position % 2 == 0)
//            convertView.setBackgroundColor(get);
//        else
//            convertView.setBackgroundColor(getContext().getColor(R.color.light_grey));

        return convertView;
    }

    //IMPLEMENT FILTER ACCORDING TO APNATUTORIALS PAGE
    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Skill skill = (Skill) resultValue;
            return skill.getSkill();
        }

        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Skill skills : tempSkill) {
                    //IF DOESN'T WORK, REPLACE CONTAINS WITH startsWith
                    if (skills.getSkill().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(skills);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Skill> s = (ArrayList<Skill>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Skill skill : s) {
                    add(skill);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
