package net.w2s.driverapp.Beans;

/**
 * Created by RWS 6 on 11/29/2016.
 */
public class DocumentBean {

    /*  "v_doc_id": 12,
            "v_id": 3,
            "school_id": 3,
            "remind_day": "13",
            "insurance_document_expiry": "2016-10-20",
            "insurance_card_copy": "http://localhost:8080/Tracking_bus/resources/dashboard/uploads/insurance_card/504828772016-07-06 15-35-15.png",
            "status": 1,
            "insurance_document_name": "asdsad"*/

    private String v_doc_id;
    private String v_id;
    private String school_id;
    private String remind_day;
    private String insurance_document_expiry;
    private String insurance_card_copy;
    private String status;
    private String insurance_document_name;

    public String getV_doc_id() {
        return v_doc_id;
    }

    public void setV_doc_id(String v_doc_id) {
        this.v_doc_id = v_doc_id;
    }

    public String getV_id() {
        return v_id;
    }

    public void setV_id(String v_id) {
        this.v_id = v_id;
    }

    public String getSchool_id() {
        return school_id;
    }

    public void setSchool_id(String school_id) {
        this.school_id = school_id;
    }

    public String getRemind_day() {
        return remind_day;
    }

    public void setRemind_day(String remind_day) {
        this.remind_day = remind_day;
    }

    public String getInsurance_document_expiry() {
        return insurance_document_expiry;
    }

    public void setInsurance_document_expiry(String insurance_document_expiry) {
        this.insurance_document_expiry = insurance_document_expiry;
    }

    public String getInsurance_card_copy() {
        return insurance_card_copy;
    }

    public void setInsurance_card_copy(String insurance_card_copy) {
        this.insurance_card_copy = insurance_card_copy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInsurance_document_name() {
        return insurance_document_name;
    }

    public void setInsurance_document_name(String insurance_document_name) {
        this.insurance_document_name = insurance_document_name;
    }
}
