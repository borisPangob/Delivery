package com.boris.delivery.driverActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.dto.ItineraryStatsDTO;
import com.boris.delivery.dto.MissionDTO;
import com.boris.delivery.service.DirectionService;
import com.boris.delivery.service.MatrixService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import android.graphics.Bitmap;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class ItineraryActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, MatrixService.MatrixServiceCallback, DirectionService.DirectionServiceCallback {
    private MapView map;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private IMapController mapController;
    private FirebaseAuth mAuth;
    private WriteBatch batch;
    private String email;
    private Button btnTerminer;
    private Timestamp timestamp;
    private LocationManager locationManager;
    private Spinner spinnerStats;;
    private String postalAddress;
    private Intent myIntent;
    private MissionDTO mission;
    private ArrayList<com.google.firebase.firestore.GeoPoint> listOfGeopoints;
    private ArrayList<String> listOfAdresses;
    private String jsonBody;
    private String jsonBodyForDistance;
    private Polyline line;
    private GeoPoint startPoint;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();
        batch = db.batch();
        myIntent = getIntent();
        String idMission = myIntent.getStringExtra("missionId");
        Log.d("itinerary", "idMission : "+idMission);

        //On récupère le contexte pour instancier la carte
        Context contex = getApplicationContext();
        Configuration.getInstance().load(contex, PreferenceManager.getDefaultSharedPreferences(contex));
        setContentView(R.layout.activity_itinerary);

        spinnerStats = findViewById(R.id.spinnerStats);
        btnTerminer = findViewById(R.id.btnTerminer);
        btnTerminer.setOnClickListener(this);
        startPoint = new GeoPoint(49.3832731, 1.0768664);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        //On centre la carte sur l'esigelec par défaut
        mapController.setCenter(startPoint);
        //Notre point de départ c'est l'esigelec [49.3832731,1.0768664],
        jsonBody ="{\"locations\":[";
        jsonBodyForDistance = "{\"coordinates\":[[1.0773112965982632, 49.38294924509391],";

        DocumentReference docMission = db.collection("mission").document(idMission);

        //Récupérer les données via une requete asynchrone
        docMission.get().addOnCompleteListener(task->{
            if(task.isSuccessful()){
                Log.d("itinerary", task.getResult().getData().toString());
                Timestamp dateTimestamp = task.getResult().getTimestamp("date");
                ArrayList<String> itemAddresses = (ArrayList<String>) task.getResult().get("listOfAddresses");
                ArrayList<com.google.firebase.firestore.GeoPoint> itemGeopoints = (ArrayList<com.google.firebase.firestore.GeoPoint>) task.getResult().get("listOfGeopoints");
                mission = new MissionDTO(dateTimestamp.toDate(),
                        task.getResult().getString("delivererEmail"),
                        task.getResult().getBoolean("isAccepted"),
                        task.getResult().getBoolean("isRealised"),
                        itemAddresses,
                        itemGeopoints,
                        task.getResult().getId());
                listOfAdresses = mission.getListOfAdrresses();
                listOfGeopoints = mission.getListOfGeopoints();
                // Supprimez les anciens marqueurs avant d'ajouter le nouveau
                map.getOverlays().removeIf(overlay -> overlay instanceof Marker);
                //On dessine l'itinéraire
                for(int i = 0; i < listOfGeopoints.size(); i++) {
                    com.google.firebase.firestore.GeoPoint geopoint = listOfGeopoints.get(i);
                    //On crée les itinéraires avant les markers pour qu'il soient en dessous
                    if(i!=0) {
                        com.google.firebase.firestore.GeoPoint geoPoint = listOfGeopoints.get(i);
                        jsonBodyForDistance += "[" + geoPoint.getLongitude() + "," + geoPoint.getLatitude() + "],";
                    }
                }
                jsonBodyForDistance =jsonBodyForDistance.substring(0, jsonBodyForDistance.length() - 1);
                jsonBodyForDistance += "]}";
                //On récupère l'itinéraire
                new DirectionService(this,jsonBodyForDistance).execute();
                line = new Polyline();
                Log.e("OpenRouteService", "jsonBody : " + jsonBody);
                // on crée des marqueur pour afficher l'itinéraire
                for(int i = 0; i < listOfGeopoints.size(); i++) {
                    com.google.firebase.firestore.GeoPoint geopoint = listOfGeopoints.get(i);;
                    jsonBody += "["+geopoint.getLongitude()+","+geopoint.getLatitude()+"],";
                    Marker userMarker = new Marker(map);
                    userMarker.setPosition(new GeoPoint(geopoint.getLatitude(), geopoint.getLongitude()));
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    userMarker.setTitle(i+" : "+listOfAdresses.get(i));
                    // On récupère l'icone de départ pour l'associer à la carte
                    Drawable vectorDrawable;
                    if(i==0){
                        vectorDrawable = getResources().getDrawable(R.drawable.ic_start);
                    }else{
                        vectorDrawable = getResources().getDrawable(R.drawable.ic_marker);
                    }
                    Bitmap iconBitmap = Bitmap.createBitmap(
                            100,
                            100,
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(iconBitmap);
                    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    vectorDrawable.draw(canvas);
                    userMarker.setIcon(new BitmapDrawable(getResources(), iconBitmap));
                    map.getOverlays().add(userMarker);
                    Log.d("itinerary", "latitude : "+geopoint.getLatitude()+" longitude : "+geopoint.getLongitude());
                    Log.d("itinerary", "addresse : "+ listOfAdresses.get(i));
                }

                jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
                jsonBody += "],\"metrics\":[\"distance\",\"duration\"]}";
                //on récupère les distances et les durées entre les trajets
                new MatrixService(this, jsonBody).execute();
            }else{
                Log.e("itinerary","Erreur :", task.getException());
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        map.onPause();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onClick(View v) {
        if(v==btnTerminer){
            Log.d("Id mission",mission.getId());
            DocumentReference docMission = db.collection("mission").document(mission.getId());
            docMission.update("isRealised", true).addOnSuccessListener(task->{
                Toast.makeText(this, "Mission Terminée", Toast.LENGTH_SHORT).show();
                Log.d("Id mission","Mission Terminée"+mission.getId());
                myIntent = new Intent(ItineraryActivity.this, DriverHistoryActivity.class);
                startActivity(myIntent);
                finish();
            });

        }

    }

    @Override
    public void onDistancesDurationsReceived(ArrayList<ItineraryStatsDTO> stats) {
        ArrayList<String> statsList = new ArrayList<String>();
        int i = 0;
        for(ItineraryStatsDTO stat : stats){
            Log.d("OpenRouteService", "stat : " + stat.toString());
            if(i<stats.size()){
                statsList.add(i+"->"+(i+1)+" : "+stat.toString());
            }
            i++;
        }
        //On crée un arrayAdapter qui utilise un tableau de String et un spinner layout par défaut
        ArrayAdapter<String> statsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statsList);
        //On spécifie le layout qui sera utilisé quand la liste des choix apparait
        statsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //On applique l'adaptateur au spinner
        spinnerStats.setAdapter(statsAdapter);
    }

    @Override
    public void onDirectionReceived(ArrayList<GeoPoint> points) {
        Log.d("OpenRouteService", "itineraryActivity : " + points.toString());
        line.setPoints(points);
        map.getOverlayManager().add(line);
    }

    @Override
    public void onError(String errorMessage) {
        Log.e("OpenRouteService", "Erreur : " + errorMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemOnHold){
            myIntent = new Intent(ItineraryActivity.this, OnHoldActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemInProgress){
            myIntent = new Intent(ItineraryActivity.this, InProgressActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(ItineraryActivity.this, DriverHistoryActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(ItineraryActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}