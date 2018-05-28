package net.w2s.driverapp.Beans;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 12/8/2016.
 */

public class NotiBean {

    private String studentId;
    private String distance;
    private ArrayList<String> parentList;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public ArrayList<String> getParentList() {
        return parentList;
    }

    public void setParentList(ArrayList<String> parentList) {
        this.parentList = parentList;
    }
}
