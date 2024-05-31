package com.boris.delivery.service;

import android.os.AsyncTask;
import android.util.Log;

import com.boris.delivery.dto.ItineraryStatsDTO;
import com.google.type.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DirectionService extends AsyncTask<Void, Void, String> {
    private String jsonBody;
    private DirectionService.DirectionServiceCallback callback;
    private ArrayList<GeoPoint> points;

    public interface DirectionServiceCallback {
        void onDirectionReceived(ArrayList<GeoPoint> points);
        void onError(String errorMessage);
    }
    public DirectionService(DirectionService.DirectionServiceCallback callback, String jsonBody){
        this.jsonBody = jsonBody;
        this.callback = callback;
        this.points = new ArrayList<GeoPoint>();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            DirectionApi(jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            // Appeler la nouvelle méthode du rappel avec les distances et les durées
            callback.onDirectionReceived(points);
            Log.w("OpenRouteService", "direction : "+ points.toString());
        } else {
            callback.onError(result);
            Log.e("OpenRouteService", "Erreur lors de la requête : " + result);
        }
    }

    public void DirectionApi(String jsonBody) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Request request = new Request.Builder()
                .url("https://api.openrouteservice.org/v2/directions/driving-car")
                .addHeader("Authorization", "5b3ce3597851110001cf6248c7ca63e6a4954307a271321c4abf4a12")
                .addHeader("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                Log.d("OpenRouteService", "directionService : " + responseBody);
                //On convertie la réponse en objet Json
                JSONObject responseJson = new JSONObject(responseBody);

                // On extrait la géométrie du premier itinéraire
                JSONArray routes = responseJson.getJSONArray("routes");
                if (routes.length() > 0) {
                    //for(int i=0;i<routes.length();i++){
                        JSONObject route = routes.getJSONObject(0);
                        String encodedPolyline = route.getString("geometry");

                        // On convertit l'encoded polyline en une liste de LatLng
                        List<com.google.android.gms.maps.model.LatLng> decodedPolyline = PolyUtil.decode(encodedPolyline);
                        for (com.google.android.gms.maps.model.LatLng latLng : decodedPolyline) {
                            points.add(new GeoPoint(latLng.latitude, latLng.longitude));
                        }
                    //}
                }else {
                    Log.e("OpenRouteService", "Aucun itinéraire trouvé.");
                }
            } else {
                Log.e("OpenRouteService", "Erreur de requête : " + response.code());
            }
        } catch (Exception e) {
            Log.e("OpenRouteService", "Erreur lors de la requête : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
