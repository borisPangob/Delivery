package com.boris.delivery.service;

import android.os.AsyncTask;
import android.util.Log;

import com.boris.delivery.dto.ItineraryStatsDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ItineraryByMatrixService extends AsyncTask<Void, Void, String> {
    private String jsonBody;
    private ItineraryByMatrixService.ItineraryByMatrixServiceCallback callback;
    private ArrayList<Integer> order;
    DecimalFormat decimalFormat;

    public interface ItineraryByMatrixServiceCallback {
        void onDistancesDurationsReceived(ArrayList<Integer> order);
        void onError(String errorMessage);
    }
    public ItineraryByMatrixService(ItineraryByMatrixServiceCallback callback, String jsonBody){
        this.jsonBody = jsonBody;
        this.callback = callback;
        this.order = new ArrayList<Integer>();
        this.decimalFormat = new DecimalFormat("#.##");
    }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            ItineraryByMatrixServiceApi(jsonBody);
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
            callback.onDistancesDurationsReceived(order);
            Log.w("OpenRouteService", "Ordre : "+order.toString());
        } else {
            callback.onError(result);
            Log.e("OpenRouteService", "Erreur lors de la requête : " + result);
        }
    }
    public void ItineraryByMatrixServiceApi(String jsonBody){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Request request = new Request.Builder()
                .url("https://api.openrouteservice.org/v2/matrix/driving-car")
                .addHeader("Authorization", "5b3ce3597851110001cf6248c7ca63e6a4954307a271321c4abf4a12")
                .addHeader("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.d("OpenRouteService", "jsonBody : " + jsonBody);
                System.out.println(responseBody);
                Log.d("OpenRouteService", "response : " + responseBody);
                //On convertie la réponse en objet Json
                JSONObject responseJson = new JSONObject(responseBody);

                // On obtient le tableau de distance
                JSONArray distancesArray = responseJson.getJSONArray("distances");
                //point de départ
                int startPoint = 0;
                order.add(startPoint);

                // Liste des points non visités
                List<Integer> unvisitedPoints = new ArrayList<>();
                for (int i = 1; i < distancesArray.length(); i++) {
                    unvisitedPoints.add(i);
                }

                // Algorithme du voyageur de commerce
                while (!unvisitedPoints.isEmpty()) {
                    int nearestPoint = findNearestPoint(startPoint, unvisitedPoints, distancesArray);
                    order.add(nearestPoint);
                    startPoint = nearestPoint;
                }

                // Affichez l'ordre des points les plus proches
                Log.d("OpenRouteService", "Ordre  : " + order.toString());


            } else {
                Log.e("OpenRouteService", "Erreur de requête : " + response.code());
            }
        } catch (Exception e) {
            Log.e("OpenRouteService", "Erreur lors de la requête : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int findNearestPoint(int startPoint, List<Integer> unvisitedPoints, JSONArray distancesArray) {
        double minDistance = Double.MAX_VALUE;
        int nearestPoint = -1;

        for (int i : unvisitedPoints) {
            double distance = 0;
            try {
                distance = distancesArray.getJSONArray(startPoint).getDouble(i);
            } catch (JSONException e) {
                Log.e("OpenRouteService", "Erreur algo voyageur de commerce : "+ e.getMessage());
            }

            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = i;
            }
        }

        unvisitedPoints.remove((Integer) nearestPoint);
        return nearestPoint;
    }
}
