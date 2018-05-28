package net.w2s.driverapp.Beans;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 11/18/2016.
 */
public class StudentBean {

    private String firstName;
    private String lastName;
    private String familyName;
    private String status;
    private String status_absent;
    private String studentId;
    private String studentContact;
    private ArrayList<ParentBean> parentList;

    public ArrayList<ParentBean> getParentList() {
        return parentList;
    }

    public void setParentList(ArrayList<ParentBean> parentList) {
        this.parentList = parentList;
    }

    public String getStudentContact() {
        return studentContact;
    }

    public void setStudentContact(String studentContact) {
        this.studentContact = studentContact;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_absent() {
        return status_absent;
    }

    public void setStatus_absent(String status_absent) {
        this.status_absent = status_absent;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }


}
