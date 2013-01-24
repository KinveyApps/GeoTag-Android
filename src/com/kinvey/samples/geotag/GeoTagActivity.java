package com.kinvey.samples.geotag;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;
import com.kinvey.MappedAppdata;
import com.kinvey.persistence.mapping.FieldConstants;
import com.kinvey.persistence.query.SimpleQuery;
import com.kinvey.util.KinveyCallback;
import com.kinvey.util.ListCallback;
import com.kinvey.util.ScalarCallback;

public class GeoTagActivity extends SherlockActivity implements
		OnMapClickListener, OnInfoWindowClickListener {

	// reference the View object which renders the map itself
	private MapView mMap = null;
	// reference the dialog used to allow user users to edit a note
	private AlertDialog mEditNote = null;
	// reference the daialog used to display legal info for using google maps
	private AlertDialog mLegalDialog = null;
	// reference to the currently selected map marker
	private Marker mCurMarker = null;
	// reference to the kinvey client, used for contacting Kinvey's BaaS
	private KCSClient mKinveyClient;
	// an instance of MappedAppdata is used to query a collection
	private MappedAppdata ma;
	// reference to EditText in note field, needed to clear
	private EditText mNote;

	public static final String COLLECTION_NAME = "mapNotes";
	private static final String TAG = GeoTagActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_tag);

		// ensure the current device can even support running google services,
		// which are required for using google maps.
		int googAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (googAvailable != ConnectionResult.SUCCESS) {
			Log.i(TAG, "googAvailable fail!");
			GooglePlayServicesUtil.getErrorDialog(googAvailable, this, 0)
					.show();
		} else {
			bindViews();
			mMap.onCreate(savedInstanceState);
			mMap.getMap().setMyLocationEnabled(true);

			setListeners();

			// loading up Kinvey app settings from the property file, located at
			// assets/kinvey.properties
			KinveySettings settings = KinveySettings
					.loadFromProperties(getApplicationContext());
			mKinveyClient = KCSClient.getInstance(getApplicationContext(),
					settings);

			ma = mKinveyClient.mappeddata(GeoTagEntity.class, COLLECTION_NAME);
			// fire off the ping call to ensure we can communicate with Kinvey
			testKinveyService();
		}

	}

	// binding xml view elements to their reference (in this sample their is
	// only one)
	private void bindViews() {
		mMap = (MapView) findViewById(R.id.geotag_map_main);
	}

	// setting the appropriate listeners, this sample will listen for clicks on
	// the map (to show a marker)
	// as well as clicks on the info window (to show dialog for creating a new
	// note)
	private void setListeners() {
		mMap.getMap().setOnMapClickListener(this);
		mMap.getMap().setOnInfoWindowClickListener(this);

	}

	/**
	 * When using google's MapView, the standard lifecycle callbacks *must* be
	 * made to the MapView.
	 * 
	 * note that onCreate() also calls mMap.onCreate(...)
	 * 
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMap != null) {
			mMap.onDestroy();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMap != null) {
			mMap.onResume();
		}
		// mMap.getMap().clear();
		// getNotesOnScreen();

	}

	// on pause makes callbacks to mapview as well as force-dismisses dialogs to
	// avoid window leaks.
	@Override
	protected void onPause() {
		super.onPause();
		if (mLegalDialog != null) {
			mLegalDialog.dismiss();
		}
		if (mEditNote != null) {
			mEditNote.dismiss();
		}
		if (mMap != null) {
			mMap.onPause();
		}
	}

	// Using ActionbarSherlock to handle options, the two are refresh and
	// legal
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_geo_tag, menu);
		return super.onCreateOptionsMenu(menu);
	}

	// depending on which option is tapped, call either to show legal or refresh
	// the dataset
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_legal:
			legal();
			return true;
		case R.id.menu_item_refresh:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void refresh() {
		getNotesOnScreen();
	}

	// lazily instantiate and pops a dialog containing legal information for
	// using google maps.
	public void legal() {
		if (mLegalDialog == null) {
			mLegalDialog = new AlertDialog.Builder(this).create();
			mLegalDialog
					.setTitle(getResources().getString(R.string.menu_legal));
			mLegalDialog.setMessage(GooglePlayServicesUtil
					.getOpenSourceSoftwareLicenseInfo(this));
			mLegalDialog.setButton(Dialog.BUTTON_POSITIVE, "Close",
					new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mLegalDialog.cancel();
						}
					});
		}
		mLegalDialog.show();
	}

	// listen for clicks on the map, clearing the current marker (if there is
	// one)
	// and setting a new marker, showing the default info window for adding a
	// note.
	@Override
	public void onMapClick(LatLng latlng) {
		if (mCurMarker != null && mCurMarker.isVisible()) {
			mCurMarker.remove();
		}
		mCurMarker = mMap.getMap().addMarker(
				new MarkerOptions()
						.position(latlng)
						.title(getResources()
								.getString(R.string.marker_default))
						.draggable(true));
		mCurMarker.showInfoWindow();

	}

	// listen for clicks on the info window, lazily instantiating the
	// "edit marker" dialog
	@Override
	public void onInfoWindowClick(final Marker mark) {
		if (mEditNote == null) {
			mEditNote = new AlertDialog.Builder(this).create();
			mEditNote.setTitle(getResources().getString(R.string.enter_note));
			View dv = getLayoutInflater().inflate(R.layout.dialog_geoloc_note,
					null);
			mNote = (EditText) dv.findViewById(R.id.geotag_note_edit);
			mEditNote.setView(dv);
			mEditNote.setButton(Dialog.BUTTON_NEGATIVE, "Cancel",
					new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mNote.setText("");

						}
					});
			mEditNote.setButton(Dialog.BUTTON_POSITIVE, "Save",
					new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							addNoteToLocation(mark.getPosition(), mNote
									.getText().toString());
						}

					});

		}

		mNote.setText("");
		mEditNote.show();
	}

	public void addNoteToLocation(LatLng loc, String note) {
		// update UI
		if (mCurMarker != null && mCurMarker.isVisible()) {
			mCurMarker.setTitle(note);
			mCurMarker.showInfoWindow();
		}
		// update kinvey
		// first create an entity to put into the collection
		GeoTagEntity curTag = new GeoTagEntity();
		curTag.setNote(note);
		curTag.setCoords(convertLatLngToLocation(loc));
		// let Kinvey auto-generate an ID for this entity

		// then save the entity to kinvey
		ma.save(curTag, new ScalarCallback<GeoTagEntity>() {

			@Override
			public void onSuccess(GeoTagEntity r) {
				String msg = String.format("Save successful%nnote: %s",
						r.getNote());
				Log.i(TAG, msg);
				Toast.makeText(GeoTagActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}

			@Override
			public void onFailure(Throwable t) {
				String msg = String.format("Save failed%nerror: %s",
						t.getMessage());
				Log.e(TAG, msg);
				Toast.makeText(GeoTagActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}

		});

	}

	public void getNotesOnScreen() {
		// first get world coordinates that are being drawn on screen
		LatLng topleft = mMap.getMap().getProjection().getVisibleRegion().farLeft;
		LatLng btmRight = mMap.getMap().getProjection().getVisibleRegion().nearRight;
		// now that we have a bounding box of what's on screen, use a
		// SimpleQuery to query Kinvey's backend `withinBox`
		SimpleQuery geoquery = new SimpleQuery();
		geoquery.withinBox(FieldConstants.KEY_GEOLOCATION, topleft.longitude,
				topleft.latitude, btmRight.longitude, btmRight.latitude);
		
//		Log.i(TAG, "topleft")
		ma.clearFilterCriteria();
		ma.setQuery(geoquery);
		ma.fetch(new ListCallback<GeoTagEntity>() {

			@Override
			public void onSuccess(List<GeoTagEntity> r) {
				String msg = "query successfull, with a size of -> " + r.size();
				Log.i(TAG, msg);
				Toast.makeText(GeoTagActivity.this, msg, Toast.LENGTH_LONG)
						.show();
				for (GeoTagEntity gte : r) {
					mMap.getMap()
							.addMarker(
									new MarkerOptions()
											.position(
													convertLocationToLatLng(gte
															.getCoords()))
											.title(gte.getNote())
											.draggable(false)).showInfoWindow();
				}
			}

			public void onFailure(Throwable t) {
				String msg = "kinvey query fetch failed, " + t.getMessage();
				Toast.makeText(GeoTagActivity.this, msg, Toast.LENGTH_LONG)
						.show();
				Log.e(TAG, msg);
			}

		});
	}

	public void testKinveyService() {
		mKinveyClient.pingService(new KinveyCallback<Boolean>() {

			public void onFailure(Throwable t) {
				Toast.makeText(GeoTagActivity.this,
						"kinvey ping failed, check assets/kinvey.properties",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onSuccess(Boolean b) {
				Toast.makeText(GeoTagActivity.this, "kinvey ping success!",
						Toast.LENGTH_LONG).show();
			}

		});

	}

	/*
	 * the below two methods are just static helper methods for translating
	 * between Location objects and LatLng objects.
	 * 
	 * Note the Google Maps API uses LatLng, while Android uses Location.
	 */

	public static Location convertLatLngToLocation(LatLng latlng) {
		Location loc = new Location(TAG);
		loc.setLatitude(latlng.latitude);
		loc.setLongitude(latlng.longitude);
		return loc;
	}

	public static LatLng convertLocationToLatLng(Location loc) {
		return new LatLng(loc.getLatitude(), loc.getLongitude());
	}

}

