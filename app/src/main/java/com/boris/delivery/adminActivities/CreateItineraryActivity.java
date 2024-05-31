package com.boris.delivery.adminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.dto.DeliveryDTO;
import com.boris.delivery.service.DirectionService;
import com.boris.delivery.service.ItineraryByMatrixService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateItineraryActivity extends AppCompatActivity implements View.OnClickListener,
        ItineraryByMatrixService.ItineraryByMatrixServiceCallback, DirectionService.DirectionServiceCallback {
    private ArrayList<DeliveryDTO> deliveryList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String chauffeur;
    private String date;
    private Button btnCreerMission;
    private Intent myIntent;
    private String jsonBody;
    private String jsonBodyForDistance;
    private Date dateMission;
    private MapView map;
    private IMapController mapController;
    private ArrayList<com.google.firebase.firestore.GeoPoint> listOfGeoPoints;
    private ArrayList<Integer> orderList;
    private com.google.firebase.firestore.GeoPoint startPoint;
    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //On récupère le contexte pour instancier la carte
        Context contex = getApplicationContext();
        Configuration.getInstance().load(contex, PreferenceManager.getDefaultSharedPreferences(contex));
        setContentView(R.layout.activity_create_itinerary);
        mAuth = FirebaseAuth.getInstance();
        btnCreerMission = findViewById(R.id.btnValider);
        btnCreerMission.setOnClickListener((View.OnClickListener) this);
        myIntent = getIntent();
        deliveryList = new ArrayList<DeliveryDTO>();
        listOfGeoPoints = new ArrayList<com.google.firebase.firestore.GeoPoint>();
        orderList = new ArrayList<Integer>();
        startPoint = new com.google.firebase.firestore.GeoPoint(49.3832731, 1.0768664);
        listOfGeoPoints.add(startPoint);
        date = myIntent.getStringExtra("date");
        chauffeur = myIntent.getStringExtra("chauffeur");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        try {
            dateMission = sdf.parse(date);
            Log.e("mission",dateMission.toString());
        } catch (Exception e) {
            Log.e("mission", "erreur conversionDate : "+e.getMessage());
        }
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        //On centre la carte sur l'esigelec par défaut
        mapController.setCenter(new GeoPoint(49.3832731, 1.0768664));
        line = new Polyline();
        //Notre point de départ c'est l'esigelec
        jsonBody ="{\"locations\":[[1.0768664,49.3832731],";
        jsonBodyForDistance = "{\"coordinates\":[[1.0773112965982632, 49.38294924509391],";

        //On récupère l'ensemble des dates de livraison
        CollectionReference deliveriesCollection = db.collection("delivery");
        deliveryList = new ArrayList<DeliveryDTO>();
        deliveriesCollection.orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DeliveryDTO delivery = new DeliveryDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Timestamp dateTimestamp = document.getTimestamp("date");
                            if(document.getTimestamp("date").toDate().equals(dateMission)) {
                                delivery = new DeliveryDTO(document.getString("address"),
                                        document.getString("clientEmail"),
                                        document.getId(),
                                        dateTimestamp.toDate(),
                                        document.getString("delivererEmail"),
                                        document.getBoolean("isAccepted"),
                                        document.getBoolean("isAttributed"),
                                        document.getGeoPoint("location"),
                                        document.getDouble("total"));
                                Log.e("TAG", document.getData().toString());
                                deliveryList.add(delivery);
                                com.google.firebase.firestore.GeoPoint geoPoint = delivery.getLocation();
                                listOfGeoPoints.add(delivery.getLocation());
                                jsonBody += "["+geoPoint.getLongitude()+","+geoPoint.getLatitude()+"],";
                            }
                        }
                        jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
                        jsonBody += "],\"metrics\":[\"distance\",\"duration\"]}";
                        //on récupère les distances et les durées entre les trajets
                        new ItineraryByMatrixService(this,jsonBody).execute();
                        Log.e("mission", jsonBody);
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
    public void onClick(View v) {
        if(v==btnCreerMission){
            if(chauffeur != null && date != null && !orderList.isEmpty()){
                //On retire le 1er élément de la liste correspondant à l'esigelec
                orderList.remove(0);
                WriteBatch batch = db.batch();
                ArrayList<String> listOfAdrresses = new ArrayList<String>();
                ArrayList<com.google.firebase.firestore.GeoPoint> geopointsOrdered = new ArrayList<com.google.firebase.firestore.GeoPoint>();
                listOfAdrresses.add("Esigelec");
                geopointsOrdered.add(startPoint);
                for(Integer i : orderList){
                    i--;
                    DeliveryDTO delivery = deliveryList.get(i);
                    if(delivery.getTimestamp().toString().equals(date)){
                        delivery.setDelivererEmail(chauffeur);
                        DocumentReference docRef = db.collection("delivery").document(delivery.getRef());
                        batch.update(docRef, "delivererEmail", chauffeur);
                        batch.update(docRef, "isAttributed", true);
                        listOfAdrresses.add(delivery.getAddress());
                        geopointsOrdered.add(delivery.getLocation());
                    }
                }
                batch.commit().addOnCompleteListener(
                        tasks->Log.d("mission","chauffeur attribué", tasks.getException())
                );
                //On récupère la date de la mission

                if(dateMission != null){
                    Timestamp timestamp = new Timestamp(dateMission);
                    System.out.println("Date récupérée : " + date.toString());
                    //On crée une nouvelle mission
                    Map<String, Object> newMission = new HashMap<>();
                    newMission.put("date", timestamp);
                    newMission.put("delivererEmail", chauffeur);
                    newMission.put("listOfAddresses", listOfAdrresses);
                    newMission.put("listOfGeopoints", geopointsOrdered);
                    newMission.put("isAccepted", false);
                    newMission.put("isRealised", false);
                    db.collection("mission").document().set(newMission)
                            .addOnSuccessListener(
                                    unused->{
                                        Toast.makeText(this, "Mission créée", Toast.LENGTH_SHORT).show();
                                        myIntent = new Intent(CreateItineraryActivity.this, HistoriqueMissionActivity.class);
                                        startActivity(myIntent);
                                        finish();
                                    })
                            .addOnFailureListener(e-> Log.e("mission", e.toString()));
                } else {
                    Log.e("mission", "Erreur creation mission, date nulle");
                }
            }else{
                Toast.makeText(this, "Données en cours de chargement", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onDistancesDurationsReceived(ArrayList<Integer> order) {
        Log.d("mission", "Ordre  : " + order.toString());
        orderList = order;
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
        int j =0;
        //On crée les itinéraires avant les markers pour qu'il soient en dessous
        for(int i = 0; i < order.size(); i++) {
            if(i!=0) {
                com.google.firebase.firestore.GeoPoint geoPoint = listOfGeoPoints.get(order.get(i));
                jsonBodyForDistance += "[" + geoPoint.getLongitude() + "," + geoPoint.getLatitude() + "],";
            }
        }
        jsonBodyForDistance =jsonBodyForDistance.substring(0, jsonBodyForDistance.length() - 1);
        jsonBodyForDistance += "]}";
        //On récupère l'itinéraire
        new DirectionService(this,jsonBodyForDistance).execute();

        for(int i : order){
            com.google.firebase.firestore.GeoPoint geopoint = listOfGeoPoints.get(i);
            Marker userMarker = new Marker(map);
            userMarker.setPosition(new GeoPoint(geopoint.getLatitude(), geopoint.getLongitude()));
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            // On récupère l'icone de départ pour l'associer à la carte
            Drawable vectorDrawable;
            if(i==0){
                userMarker.setTitle(j+" : Esigelec");
                vectorDrawable = getResources().getDrawable(R.drawable.ic_start);
            }else{
                userMarker.setTitle(j+" : "+deliveryList.get(i-1).getAddress());
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
            j++;
        }

    }

    @Override
    public void onDirectionReceived(ArrayList<GeoPoint> points) {
        Log.d("OpenRouteService", "CreateItineraryActivity : " + points.toString());
        line.setPoints(points);
        map.getOverlayManager().add(line);
    }

    @Override
    public void onError(String errorMessage) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemMission){
            myIntent = new Intent(CreateItineraryActivity.this, CreateMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(CreateItineraryActivity.this, HistoriqueMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHome){
            myIntent = new Intent(CreateItineraryActivity.this, MenuPlanificateurActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(CreateItineraryActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}