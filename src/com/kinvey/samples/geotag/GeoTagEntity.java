/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
