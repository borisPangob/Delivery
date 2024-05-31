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
import com.boris.delivery.adapter.DeliveryAdapter;
import com.boris.delivery.dto.DeliveryDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuPlanificateurActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewDelivery ;
    private List<DeliveryDTO> items;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_planificateur);
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();
        CollectionReference deliveriesCollection = db.collection("delivery");
        items = new ArrayList<DeliveryDTO>();
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
                        items.add(delivery);
                    }
                }
                recyclerViewDelivery = findViewById(R.id.recyclerViewDelivery);
                recyclerViewDelivery.setLayoutManager(new LinearLayoutManager(this));
                DeliveryAdapter adapter = new DeliveryAdapter(items);
                recyclerViewDelivery.setAdapter(adapter);
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
            myIntent = new Intent(MenuPlanificateurActivity.this, CreateMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(MenuPlanificateurActivity.this, HistoriqueMissionActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHome){
            myIntent = new Intent(MenuPlanificateurActivity.this, MenuPlanificateurActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(MenuPlanificateurActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}