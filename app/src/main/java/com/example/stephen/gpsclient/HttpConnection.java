package com.example.stephen.gpsclient;

import android.location.Location;
import android.os.AsyncTask;
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
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Stephen on 26/04/2016.
 */
public class HttpConnection extends AsyncTask<String, Void, String> {

    private String url;
    private String name;
    private Location location;

    public HttpConnection(String url) {
        this.url = url;
        //this.name = name;
        //this.location = location;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String response = null;

        //Setup Http connection
        try {
            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            //Build JSON object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "gary");
            //jsonObject.put("longitude", this.location.getLongitude());
            //jsonObject.put("latitude", this.location.getLatitude());

            //Convert to string
            String json = jsonObject.toString();

            //Send JSON object
            Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            writer.write(json);
            writer.close();

            //Receive Response
            InputStream inputStream = urlConnection.getInputStream();

            //If no response
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

        } catch (ProtocolException e) {
            e.printStackTrace();
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

        return null;
        }
    }
}
