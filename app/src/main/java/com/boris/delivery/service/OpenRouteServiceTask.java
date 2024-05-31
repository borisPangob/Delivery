package com.boris.delivery.service;

import android.os.AsyncTask;
import android.util.Log;

import com.boris.delivery.deliveryInterface.OpenRouteServiceAPI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenRouteServiceTask extends AsyncTask<String, Void, String> {
    private static final String API_KEY = "5b3ce3597851110001cf6248c7ca63e6a4954307a271321c4abf4a12";
    private static final String BASE_URL = "https://api.openrouteservice.org/";
    private static final String PROFILE = "driving-car";

    private OpenRouteServiceCallback callback;

    public interface OpenRouteServiceCallback {
        void onRouteReceived(String routeJson);
        void onError(String errorMessage);
    }

    public OpenRouteServiceTask(OpenRouteServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        String startCoordinates = strings[0];
        String endCoordinates = strings[1];
        try {
            // Construction de l'URL de l'API OpenRouteService
            String apiUrl = BASE_URL + "v2/directions/" + PROFILE + "?api_key=" + API_KEY +"&start="+ startCoordinates + "&end=" + endCoordinates;
            Log.e("OpenRouteService", "apiUrl : " + apiUrl);
            // Utilisation de Retrofit pour effectuer la requête HTTP
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            OpenRouteServiceAPI service = retrofit.create(OpenRouteServiceAPI.class);
            Call<String> call = service.getRoute(apiUrl);

            // Exécution de la requête
            Response response = call.execute().raw();

            // Traitement de la réponse
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e("OpenRouteService", "Erreur de requête : " + response.code());
                return null;
            }
        } catch (Exception e) {
            Log.e("OpenRouteService", "Erreur lors de la requête : " + e.getMessage());
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            // Traitement de la réponse JSON
            JsonObject jsonResponse = JsonParser.parseString(result).getAsJsonObject();
            callback.onRouteReceived(result);
            // Faites ce que vous voulez avec les données de l'itinéraire ici
            Log.d("OpenRouteService", "Réponse : " + jsonResponse.toString());
        }else {
            callback.onError("Erreur lors de la requête");
        }
    }
}
