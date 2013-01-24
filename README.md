GeoTag is a sample application utilizing Google Map's API with Kinvey's Backend to allow users to both add and view annotations on a map.

## Get it running

There are two pre-requirements for getting this project to run:

1.  Get your app-key and app-secret from the Kinvey console, by signing up and then creating a new app.
2.  Get a (free) Android google maps v2 API key by following the instructions listed here:
`https://developers.google.com/maps/documentation/android/start#creating_an_api_project`

Once you have an app-key, an app-secret, and a google Maps API key, let's get this sample running!

Open up the file `AndroidManifest.xml` and scroll down to the bottom.  We are looking for:

    <meta-data
        android:name="com.google.android.maps.v2.API_KEY"
        android:value="__GOOGLE_MAPS_API_KEY__" />

You guessed it, we are gonna be replacing `__GOOGLE_MAPS_API_KEY__` with the Key for Android App with Certificate you got from Google.  Make sure you leave the `""` in the manifest around your key.




Then, within the project, look for `assets/kinvey.properties`.  This property file maintains your Kinvey application credentials, and these need to be updated with your applications app-key and app-secret.

    app.key=__KINVEY_APP_KEY__
    app.secret=__KINVEY_APP_SECRET__
    
Just replace `__KINVEY_APP_KEY__` with your app-key and `__KINVEY_APP_SECRET__` with your app-secret.



####And that's it.



## Functionality
This application demonstrates:

* Storing and retreiving data from a collection
* Simple geolocation querying
* Integretiong with Kinvey, Google Maps API, and ActionBarSherlock

## Design

This sample utilizes both Google's support v4 package as well as Jake Wharton's ActionBarSherlock.  These dependencies are included, and should not require any configuration by the user (except the above mentioned API keys)

