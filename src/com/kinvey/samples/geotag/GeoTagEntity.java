/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.geotag;


import android.location.Location;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author edwardf
 * @since 2.0
 */
public class GeoTagEntity extends GenericJson {

    @Key("_id")
    private String objectId;
    @Key("note")
    private String note;
    @Key("_geoloc")
    private double[] coords;

    public GeoTagEntity() {}

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String id) {
        this.objectId = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String nt) {
        this.note = nt;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

}
