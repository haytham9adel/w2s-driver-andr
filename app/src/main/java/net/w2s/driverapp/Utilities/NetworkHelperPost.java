package net.w2s.driverapp.Utilities;

import android.net.http.HttpResponseCache;

import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelperPost {
    HttpResponseCache response ;
    HttpURLConnection client;
    String serviceURL;
    //    JSONObject responseObject;
    String responseString;

    public NetworkHelperPost(String serviceURL) {
        this.serviceURL = serviceURL;
    }


    public String executePostRequest(){
        return executePostRequest(null);
    }

    public String executePostRequest(JSONStringer postParameters){
        StringBuffer response= new StringBuffer();
        try {
            URL myURL = new URL(serviceURL);
            HttpURLConnection myURLConnection = (HttpURLConnection) (myURL).openConnection();
            myURLConnection.setRequestMethod("POST");
            myURLConnection.setRequestProperty("Content-Type", "application/json");
            myURLConnection.setUseCaches(false);
            myURLConnection.setDoInput(true);
            myURLConnection.setDoOutput(true);
            OutputStream os = myURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postParameters.toString());
            writer.flush();
            writer.close();
            os.close();
            myURLConnection.connect();
            // 6. Get the response
//            int responseCode = myURLConnection.getResponseCode();
//            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. Print result
//            System.out.println(response.toString());

        }catch (Exception e){
            return "";
        }
        return response.toString();
    }

}
