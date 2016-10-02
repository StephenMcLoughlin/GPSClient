package com.example.stephen.gpsclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;


    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;

    //May delete
    private String name;
    private String ipAddress;

    private TextView locationtxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        ipAddress = intent.getStringExtra("ip");

        locationtxt = (TextView) findViewById(R.id.locationTxt);

        //Setup Google Api Client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Initialise location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public void onConnected(Bundle bundle) {

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(GPSActivity.this, "Location changed: " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        new HttpConnection(this.name, location, this.ipAddress).execute();
    }

    private class HttpConnection extends AsyncTask<Void, Void, String> {

        private String name;
        private Location location;
        private String ipAddress;

        public HttpConnection(String name, Location location, String ipAddress) {

            this.name = name;
            this.location = location;
            this.ipAddress = ipAddress;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String URL = "http://" + this.ipAddress + ":8080/RESTfulServiceGPSServlet/rest/userlocation/post";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String response = null;

            try {
               java.net.URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");


                //Build JSON object
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", this.name);
                jsonObject.put("longitude", this.location.getLongitude());
                jsonObject.put("latitude", this.location.getLatitude());
                jsonObject.put("time", getTimeStamp());

                String json = jsonObject.toString();

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                writer.write(json);
                writer.close();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                response = buffer.toString();

                return response;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {

                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            locationtxt.setText("Location: " + response);
        }

    }

    private String getTimeStamp() {

        Long tsLong = System.currentTimeMillis()/1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeDate = sdf.format(sdf.toString());
        return timeDate;
    }

}

