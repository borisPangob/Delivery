package com.boris.delivery.driverActivities;

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
import com.boris.delivery.adapter.InProgressAdapter;
import com.boris.delivery.deliveryInterface.OnItemClickListener;
import com.boris.delivery.dto.DeliveryDTO;
import com.boris.delivery.dto.MissionDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class DriverHistoryActivity extends AppCompatActivity implements OnItemClickListener {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewInProgress ;
    private List<MissionDTO> items;
    private List<DeliveryDTO> deliveryList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private Intent myIntent;
    private InProgressAdapter adapter;
    private WriteBatch batch;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_history);
        mAuth = FirebaseAuth.getInstance();
        batch = db.batch();
        email = mAuth.getCurrentUser().getEmail();
        CollectionReference missionsCollection = db.collection("mission");
        items = new ArrayList<MissionDTO>();
        missionsCollection.orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MissionDTO mission = new MissionDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(document.getBoolean("isAccepted")
                                    && document.getBoolean("isRealised")
                                    && document.getString("delivererEmail").equals(email)) {
                                Timestamp dateTimestamp = document.getTimestamp("date");
                                ArrayList<String> listOfAdrresses = (ArrayList<String>) document.get("listOfAddresses");
                                ArrayList<GeoPoint> listOfGeopoints = (ArrayList<GeoPoint>) document.get("listOfGeopoints");
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
                        }
                        recyclerViewInProgress = findViewById(R.id.recyclerViewInProgress);
                        recyclerViewInProgress.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new InProgressAdapter(items, this);
                        recyclerViewInProgress.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onItemClick(int position) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemOnHold){
            myIntent = new Intent(DriverHistoryActivity.this, OnHoldActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemInProgress){
            myIntent = new Intent(DriverHistoryActivity.this, InProgressActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(DriverHistoryActivity.this, DriverHistoryActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(DriverHistoryActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}