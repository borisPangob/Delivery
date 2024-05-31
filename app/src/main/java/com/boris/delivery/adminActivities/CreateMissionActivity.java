package com.boris.delivery.adminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.dto.DeliveryDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreateMissionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    Spinner spinnerChauffeur;
    Spinner spinnerDate;
    private String chauffeur;
    private String date;
    private ArrayList<String> driverList;
    private ArrayList<String> dateList;
    private ArrayList<DeliveryDTO> deliveryList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private Button btnCreerItineraire;
    private Intent myIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mission);
        mAuth = FirebaseAuth.getInstance();
        btnCreerItineraire = findViewById(R.id.createItineraire);
        spinnerChauffeur = findViewById(R.id.spinnerChauffeur);
        spinnerDate = findViewById(R.id.spinnerDate);

        btnCreerItineraire.setOnClickListener(this);
        spinnerChauffeur.setOnItemSelectedListener(this);
        spinnerDate.setOnItemSelectedListener(this);
        //On récupère la liste des chauffeurs
        CollectionReference usersCollection = db.collection("utilisateurs");
        driverList = new ArrayList<String>();
        usersCollection.whereEqualTo("role",20)//on récupère notre liste de chauffeurs
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            driverList.add(document.getString("email"));
                        }

                    }
                    //On crée un arrayAdapter qui utilise un tableau de String et un spinner layout par défaut
                    ArrayAdapter<String> DriverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, driverList);
                    //On spécifie le layout qui sera utilisé quand la liste des choix apparait
                    DriverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //On applique l'adaptateur au spinner
                    spinnerChauffeur.setAdapter(DriverAdapter);
                    chauffeur = driverList.get(0);
                });


        //On récupère l'ensemble des dates de livraison
        CollectionReference deliveriesCollection = db.collection("delivery");
        deliveryList = new ArrayList<DeliveryDTO>();
        deliveriesCollection.orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DeliveryDTO delivery = new DeliveryDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(!document.getBoolean("isAccepted") && !document.getBoolean("isAttributed")) {
                                Timestamp dateTimestamp = document.getTimestamp("date");
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
                            }
                        }
                    }
                    //On utilise un HashSet afin d'éviter les doublons
                    Set<String> dateSet = new HashSet<>();
                    for (DeliveryDTO delivery : deliveryList) {
                        dateSet.add(delivery.getTimestamp().toString());
                    }
                    // On convertie le HashSet en ArrayList
                    dateList = new ArrayList<>(dateSet);
                    //On crée un arrayAdapter qui utilise un tableau de String et un spinner layout par défaut
                    ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateList);
                    //On spécifie le layout qui sera utilisé quand la liste des choix apparait
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //On applique l'adaptateur au spinner
                    spinnerDate.setAdapter(dateAdapter);
                    date = dateList.get(0);
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int viewId = parent.getId();

        if (viewId == R.id.spinnerChauffeur) {
            chauffeur = driverList.get(position);
        } else if (viewId == R.id.spinnerDate) {
            date = dateList.get(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if(v == btnCreerItineraire){
            myIntent = new Intent(CreateMissionActivity.this, CreateItineraryActivity.class);
            myIntent.putExtra("date", date);
            myIntent.putExtra("chauffeur", chauffeur);
            startActivity(myIntent);
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemMission){
            myIntent = new Intent(CreateMissionActivity.this, CreateMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(CreateMissionActivity.this, HistoriqueMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHome){
            myIntent = new Intent(CreateMissionActivity.this, MenuPlanificateurActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(CreateMissionActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}