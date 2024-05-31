package com.boris.delivery.service;

import android.os.AsyncTask;
import android.util.Log;

import com.boris.delivery.dto.ItineraryStatsDTO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MatrixService extends AsyncTask<Void, Void, String> {
    private String jsonBody;
    private MatrixService.MatrixServiceCallback callback;
    private ArrayList<ItineraryStatsDTO> stats;
    DecimalFormat decimalFormat;

    public interface MatrixServiceCallback {
        void onDistancesDurationsReceived(ArrayList<ItineraryStatsDTO> stats);
        void onError(String errorMessage);
    }
    public MatrixService(MatrixServiceCallback callback, String jsonBody){
        this.jsonBody = jsonBody;
        this.callback = callback;
        this.stats = new ArrayList<ItineraryStatsDTO>();
        this.decimalFormat = new DecimalFormat("#.##");
    }
    @Override
    protected String doInBackground(Void... voids) {
        try {
            MatrixApi(jsonBody);
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
            callback.onDistancesDurationsReceived(stats);
            Log.w("OpenRouteService", "Stats : "+stats.toString());
        } else {
            callback.onError(result);
            Log.e("OpenRouteService", "Erreur lors de la requête : " + result);
        }
    }
    public void MatrixApi(String jsonBody){
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
                System.out.println(responseBody);
                Log.d("OpenRouteService", "response : " + responseBody);
                //On convertie la réponse en objet Json
                JSONObject responseJson = new JSONObject(responseBody);

                // On obtient le tableau de durées
                JSONArray durationsArray = responseJson.getJSONArray("durations");
                // On obtient le tableau de distance
                JSONArray distancesArray = responseJson.getJSONArray("distances");

                // On parcours le tableau et on récupère les valeurs
                if(durationsArray.length() == distancesArray.length()) {
                    for (int i = 0; i < durationsArray.length()-1; i++) {
                        JSONArray durationRow = durationsArray.getJSONArray(i);
                        JSONArray distanceRow = distancesArray.getJSONArray(i);
                        int j = i+1;
                        double duration = durationRow.getDouble(j);
                        int minutes = (int) duration/60;
                        int seconds = (int) ((duration - minutes*60));
                        double distance = distanceRow.getDouble(j)/1000;
                        stats.add(new ItineraryStatsDTO(String.valueOf(decimalFormat.format(distance))+"km",
                                String.valueOf(minutes)+"min"+String.valueOf(seconds)+"sec"));
                        Log.d("OpenRouteService", "Durée entre le point " + i + " et le point " + j + " : " + duration + " secondes");
                        Log.d("OpenRouteService","Distance entre le point " + i + " et le point " + j + " : " + distance + " mètres");
                    }
                }else{
                    Log.e("OpenRouteService", "le nombre de distances : "+distancesArray.length() +" est != du nombre de duréees : "+durationsArray.length());
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
