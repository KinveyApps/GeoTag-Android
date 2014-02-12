GeoTag is a sample application utilizing Google Map's API with Kinvey's Backend to allow users to both add and view annotations on a map.

## Get it running

There are some initial steps required for getting this project to run:

1.  [Download Kinvey's Android Library](http://devcenter.kinvey.com/android/downloads) and get your app-key and app-secret from the Kinvey console, by signing up and then creating a new app.\
2.  Get a (free) Android google maps v2 API key by following the instructions listed here:
`https://developers.google.com/maps/documentation/android/start#creating_an_api_project`
3.  Download [ActionBarSherlock](http://actionbarsherlock.com/) and in Eclipse, go `File` -> `New` -> `Android Project from Existing Source` and navigate to the ActionBarSherlock directory.
4.  In Eclipse, go `File` -> `New` -> `Android project from Existing Source`, and navigate to the directory you downloaded the Android SDK, then goto: <sdkDirectory>/extras/google/google_play_services/lib_project and import google-play_services_lib as a project into eclipse.
5.  Navigate to <sdkDirectory>/extras/google/google_play_services/lib_project and import google-play_services_lib/libs/ and copy the google-play-services.jar into the /libs/ directory of the GeoTag project in Eclipse.
5.  After importing `GeoTag`, right click on the project -> `Properties` -> `Android`, and in the `Library` section Add both ActionBarSherlock and the Google Play Services. 
6.  Extract the zip downloaded in step 1, and place the contents of the /libs/ directory into the libs/ directory of the GeoTag Project.
8.  Right click on the Project -> `Properties` -> `Java Build Path` -> `Projects` Tab, and add ActionBarSherlock and Google-Play_services_lib.





Once you have all dependencies, an app-key, an app-secret, and a google Maps API key, let's get this sample running!



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
* Integrating with Kinvey, Google Maps API, and ActionBarSherlock

## Design

This sample utilizes both Google's support v4 package as well as Jake Wharton's ActionBarSherlock.  These dependencies are included, and should not require any configuration by the user (except the above mentioned API keys)


##License


Copyright (c) 2014 Kinvey Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.