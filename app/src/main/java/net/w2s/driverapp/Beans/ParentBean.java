package net.w2s.driverapp.Beans;

/**
 * Created by RWS 6 on 11/21/2016.
 */
public class ParentBean {

    /*
    *   "parent_number": "9981472471",
                    "parent_family_name": "Singhal",
                    "parent_fname": "Prakash1"
    * */

    public String parent_number;
    public String parent_family_name;
    public String parent_fname;
    private String speed;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    private String relationship;

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public void setNotiSend(boolean notiSend) {
        isNotiSend = notiSend;
    }

    private String country_code;
    boolean isNotiSend;

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public boolean isNotiSend() {
        return isNotiSend;
    }

    public void setIsNotiSend(boolean isNotiSend) {
        this.isNotiSend = isNotiSend;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String parent_id;

    public String getParent_number() {
        return parent_number;
    }

    public void setParent_number(String parent_number) {
        this.parent_number = parent_number;
    }

    public String getParent_family_name() {
        return parent_family_name;
    }

    public void setParent_family_name(String parent_family_name) {
        this.parent_family_name = parent_family_name;
    }

    public String getParent_fname() {
        return parent_fname;
    }

    public void setParent_fname(String parent_fname) {
        this.parent_fname = parent_fname;
    }
}
