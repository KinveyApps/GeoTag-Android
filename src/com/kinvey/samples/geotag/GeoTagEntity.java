package com.kinvey.samples.geotag;

import java.util.Arrays;
import java.util.List;

import android.location.Location;

import com.kinvey.persistence.mapping.FieldConstants;
import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class GeoTagEntity implements MappedEntity {

	private String objectId;
	private String note;
	private Location coords;
	
	public GeoTagEntity(){
		
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] {
				new MappedField("note", "note"),
				new MappedField("coords", FieldConstants.KEY_GEOLOCATION),
				new MappedField("objectId", FieldConstants.KEY_ID) });
	}

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

	public Location getCoords() {
		return coords;
	}

	public void setCoords(Location coords) {
		this.coords = coords;
	}

}
