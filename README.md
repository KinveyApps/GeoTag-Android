GeoTag is a sample application utilizing Google Map's API with Kinvey's Backend to allow users to both add and view annotations on a map.

## Set up Geotag Project
1. Clone this repo
2. Get a (free) Android google maps v2 API key by following the instructions listed here:
`https://developers.google.com/maps/documentation/android/start#creating_an_api_project`

###Android Studio
1. In Android Studio, go to **File &rarr; New &rarr; Import Project**
2. **Browse** to the extracted zip from step 1, and click **OK**
3. Click **Next** and **Finish**.
4.  Click the **play** button to start a build, if you still see compilation errors ensure the versions are correctly defined in the dependencies list

###Finally, for all IDEs
5. Specify your app key and secret in `assets/kinvey.properties` constant variables
`app.key` and `app.secret`


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
* Integrating with Kinvey, Google Maps API. This project is written in `Kotlin`.

## Design

This sample utilizes both Google's AndroidX standard support package as well as Google Services Maps API. These dependencies are included, and should not require any configuration by the user (except the above mentioned API keys)

##License


Copyright (c) 2019 Kinvey Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.