package com.skillsearch.skillsearch;

/**
 * Created by COld on 6/03/2017.
 */

public class Skill {
    private String skill;
    private String category;

    public Skill (String skill, String category) {
        this.skill = skill;
        this.category = category;
    }

    public String getSkill() {
        return this.skill;
    }
    public String getCategory() {
        return this.category;
    }
    public void setSkill(String skill) {
        this.skill = skill;
    }
    public void setCategory(String category) {
        this.category = category;
    }
}
