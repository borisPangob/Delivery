package com.boris.delivery.clientActivities;

import static android.app.ProgressDialog.show;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.dto.OrderDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    private MapView map;
    private IMapController mapController;
    private FirebaseAuth mAuth;
    private String email;
    private Button btnEnvoyer;
    private Button btnLocaliser;
    private CalendarView calendarView;
    private Timestamp timestamp;
    private LocationManager locationManager;
    private EditText postalAddressEdittext;
    private String postalAddress;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();

        //On récupère le contexte pour instancier la carte
        Context contex = getApplicationContext();
        Configuration.getInstance().load(contex, PreferenceManager.getDefaultSharedPreferences(contex));
        setContentView(R.layout.activity_map);
        postalAddressEdittext = findViewById(R.id.postalAddress);
        btnEnvoyer = findViewById(R.id.envoyer);
        btnEnvoyer.setOnClickListener(this);
        btnEnvoyer.setVisibility(View.INVISIBLE);
        btnLocaliser = findViewById(R.id.localiser);
        btnLocaliser.setOnClickListener(this);
        calendarView = findViewById(R.id.calendarView);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(18.5);
        //On centre la carte sur l'esigelec par défaut
        GeoPoint startPoint = new GeoPoint(49.3832731, 1.0768664);
        mapController.setCenter(startPoint);

        // Vérifiez si la permission est accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.getLocation();
        } else {
            // La permission n'est pas accordée, demandez-la à l'utilisateur
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
        // On crée un objet Calendar et on définit la date sélectionnée
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //on met la date en français
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
        String formattedDate = sdf.format(calendar.getTime());
        //On récupère la date par défaut
        timestamp = new Timestamp(calendar.getTime());
        //On récupère la date modifiée
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // On crée un nouveau Calendar et on le met à jour avec la nouvelle date sélectionnée
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                newCalendar.set(Calendar.MILLISECOND, 0);
                //on met la date en français
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                String formattedDate = sdf.format(newCalendar.getTime());
                System.out.println("Date en français : " + formattedDate);
                timestamp = new Timestamp(newCalendar.getTime());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent myIntent;
        if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(MapActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }else if (item.getItemId() ==R.id.itemHome) {
            myIntent = new Intent(MapActivity.this, MenuClientActivity.class);
            startActivity(myIntent);
            finish();
        }
        else if (item.getItemId() ==R.id.itemCart) {
            myIntent = new Intent(MapActivity.this, PanierActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique) {
            myIntent = new Intent(MapActivity.this, ClientHistoryActivity.class);
            startActivity(myIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.getLocation();
            } else {
                Log.w("UserLocation", "Permission Denied");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnEnvoyer) {
            //On récupère la date et l'addresse de la livraison
            EditText postalAddressEditText = findViewById(R.id.postalAddress);
            String postalAddress = postalAddressEditText.getText().toString();
            Intent intent = getIntent();
            Double total = intent.getDoubleExtra("total",0.0);
            if(timestamp.toDate().after(new Date())) {
                //On crée une nouvelle livraison
                Map<String, Object> newDelivey = new HashMap<>();
                newDelivey.put("isAccepted", false);
                newDelivey.put("isAttributed", false);
                newDelivey.put("total", total);
                newDelivey.put("date", timestamp);
                newDelivey.put("clientEmail",  email);
                newDelivey.put("delivererEmail", "");
                newDelivey.put("address", postalAddress);
                newDelivey.put("location", new com.google.firebase.firestore.GeoPoint(latitude, longitude));
                //On récupère l'Id du document
                String newDocumentId = db.collection("delivery").document().getId();
                db.collection("delivery").document(newDocumentId).set(newDelivey).addOnSuccessListener(
                                unused-> Toast.makeText(this, "Commandes enregistrées pour livraison", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e-> Log.e("delivery", e.toString()));
                //On associe les commandes à une livraison
                WriteBatch batch = db.batch();
                ArrayList<OrderDTO> itemOrders = (ArrayList<OrderDTO>) intent.getSerializableExtra("orderList");
                for(OrderDTO order : itemOrders) {
                    DocumentReference docRef = db.collection("order").document(order.getId());
                    batch.update(docRef, "deliveryId", newDocumentId);
                    batch.update(docRef, "inCart", false);
                    batch.update(docRef, "inDelivery", true);
                }

                batch.commit().addOnCompleteListener(
                        tasks->Log.d("livraison","livraison associée", tasks.getException())
                );
                //On redirige vers la page accueil Client
                intent = new Intent(MapActivity.this, ClientHistoryActivity.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(this, "Choisissez une date de livraison valide", Toast.LENGTH_SHORT).show();
            }

        } else if (v == btnLocaliser) {
            String address = postalAddressEdittext.getText().toString();
            this.getCoordinatesFromAddress(address);
            if (latitude !=0.0 && longitude !=0.0){
                btnEnvoyer.setVisibility(View.VISIBLE);
            }

        }

    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            // On récupère la dernière position connue du GPS_PROVIDER
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //On vérifie si la dernière position est disponible
            if (lastKnownLocation != null) {
                updateLocationInfo(lastKnownLocation);
            } else {
                // Si la dernière position n'est pas disponible, on demande des mises à jour de localisation
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MapActivity.this);
            }
            // On demande des mises à jour de localisation
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // On Arrête les mises à jour après avoir reçu la première mise à jour
                    locationManager.removeUpdates(this);

                    // Traitement de la localisation
                    updateLocationInfo(location);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Mise à jour de l'information de localisation lorsque la position change
        updateLocationInfo(location);
    }

    public void updateLocationInfo(Location location) {
        try {
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            postalAddressEdittext.setText(address);
            mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));

            // on crée un marqueur pour afficher la localisation de l'utilisateur
            Marker userMarker = new Marker(map);
            userMarker.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            userMarker.setTitle("Votre position");

            // Supprimez les anciens marqueurs avant d'ajouter le nouveau
            map.getOverlays().removeIf(overlay -> overlay instanceof Marker);
            map.getOverlays().add(userMarker);

            // Si vous ne souhaitez obtenir la position qu'une seule fois, arrêtez les mises à jour de localisation après la première mise à jour
            locationManager.removeUpdates(MapActivity.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCoordinatesFromAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(address, 1);

            if (addresses != null && !addresses.isEmpty()) {
                latitude = addresses.get(0).getLatitude();
                longitude = addresses.get(0).getLongitude();

                Toast.makeText(MapActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();

                mapController.setCenter(new GeoPoint(latitude, longitude));
                // on crée un marqueur pour afficher l'adresse de livraison
                Marker userMarker = new Marker(map);
                userMarker.setPosition(new GeoPoint(latitude, longitude));
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                userMarker.setTitle("Adresse de livraison");

                // On supprime les anciens marqueurs avant d'ajouter le nouveau
                map.getOverlays().removeIf(overlay -> overlay instanceof Marker);
                map.getOverlays().add(userMarker);
            } else {
                Toast.makeText(MapActivity.this, "Aucune coordonnée trouvée pour l'adresse spécifiée.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MapActivity.this, "Erreur lors de la conversion de l'adresse en coordonnées.", Toast.LENGTH_SHORT).show();
        }
    }
}