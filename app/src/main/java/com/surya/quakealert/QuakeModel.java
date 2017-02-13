package com.surya.quakealert;

/**
 * Created by Surya on 14-02-2017.
 */
public class QuakeModel {

    private Double mag;
    private String place;
    private Long time;
    private String detail;
    private String felt;

    public QuakeModel(Double mag, String place, Long time, String detail, String felt) {
        this.mag = mag;
        this.place = place;
        this.time = time;
        this.detail = detail;
        this.felt = felt;
    }

    public Double getMag() {
        return mag;
    }

    public String getPlace() {
        return place;
    }

    public Long getTime() {
        return time;
    }

    public String getDetail() {
        return detail;
    }

    public String getFelt() {
        return felt;
    }
}
