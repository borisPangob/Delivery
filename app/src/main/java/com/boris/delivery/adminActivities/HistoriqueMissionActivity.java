package com.boris.delivery.adminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.adapter.MissionAdapter;
import com.boris.delivery.dto.MissionDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoriqueMissionActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewMission ;
    private List<MissionDTO> items;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_mission);

        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();
        CollectionReference missionsCollection = db.collection("mission");
        items = new ArrayList<MissionDTO>();
        missionsCollection.orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MissionDTO mission = new MissionDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                                Timestamp dateTimestamp = document.getTimestamp("date");
                                ArrayList<String> listOfAdrresses = (ArrayList<String>) document.get("listOfAddresses");
                                ArrayList<GeoPoint> listOfGeopoints = (ArrayList<GeoPoint>)document.get("listOfGeopoints");
                                mission = new MissionDTO(dateTimestamp.toDate(),
                                        document.getString("delivererEmail"),
                                        document.getBoolean("isAccepted"),
                                        document.getBoolean("isRealised"),
                                        listOfAdrresses,
                                        listOfGeopoints,
                                        document.getId());
                                Log.e("TAG", document.getData().toString());
                                items.add(mission);
                        }
                        recyclerViewMission = findViewById(R.id.recyclerViewMission);
                        recyclerViewMission.setLayoutManager(new LinearLayoutManager(this));
                        MissionAdapter adapter = new MissionAdapter(items);
                        recyclerViewMission.setAdapter(adapter);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemMission){
            myIntent = new Intent(HistoriqueMissionActivity.this, CreateMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(HistoriqueMissionActivity.this, HistoriqueMissionActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemHome){
            myIntent = new Intent(HistoriqueMissionActivity.this, MenuPlanificateurActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(HistoriqueMissionActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}