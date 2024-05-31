package com.boris.delivery.driverActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.adapter.OnHoldAdapter;
import com.boris.delivery.deliveryInterface.OnButtonClickListener;
import com.boris.delivery.dto.DeliveryDTO;
import com.boris.delivery.dto.MissionDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class OnHoldActivity extends AppCompatActivity implements OnButtonClickListener {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewOnHold ;
    private List<MissionDTO> items;
    private List<DeliveryDTO> deliveryList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;
    private Intent myIntent;
    private OnHoldAdapter adapter;
    private WriteBatch batch;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_hold);

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
                            if(!document.getBoolean("isAccepted")
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
                        recyclerViewOnHold = findViewById(R.id.recyclerViewOnHold);
                        recyclerViewOnHold.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new OnHoldAdapter(items, this);
                        recyclerViewOnHold.setAdapter(adapter);
                    }
                });
        CollectionReference deliveriesCollection = db.collection("delivery");
        deliveryList = new ArrayList<DeliveryDTO>();
        deliveriesCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DeliveryDTO delivery = new DeliveryDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Timestamp dateTimestamp = document.getTimestamp("date");
                            if(document.getString("delivererEmail").equals(email)
                                    && document.getBoolean("isAttributed")) {
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
                });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_LOCATION_PERMISSION);
        }else {
            FirebaseMessaging.getInstance().subscribeToTopic("nouvelle_mission")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.w("notif", "erreur d'envoie");
                            } else {
                                Log.w("notif", "envoie réussie");
                            }
                        }
                    });
        }
    }

    @Override
    public void onDeleteClick(int position) {

    }

    @Override
    public void onIncreaseClick(int position) {

    }

    @Override
    public void onDecreaseClick(int position) {

    }

    @Override
    public void onValidateClick(int position) {
        if (position >= 0 && position < items.size()) {
            MissionDTO mission = items.get(position);
            Log.d("Id mission",mission.getId());
            DocumentReference docRef = db.collection("mission").document(mission.getId());
            batch.update(docRef, "isAccepted", true);

            //On récupère la liste des livraisons
            for (DeliveryDTO delivery : deliveryList){
                if(delivery.getTimestamp().toString().equals(mission.getDate().toString())) {
                    DocumentReference deliveryDocRef = db.collection("delivery").document(delivery.getRef());
                    Log.e("delivery Id", delivery.getRef());
                    batch.update(deliveryDocRef, "isAccepted", true);
                }
            }
            batch.commit().addOnCompleteListener(
                    tasks->{
                        Log.d("delivery","Mission Acceptée!", tasks.getException());
                        Toast.makeText(this, "Mission Acceptée! ", Toast.LENGTH_SHORT).show();
                        // On retire la mission de la liste en attente
                        items.remove(position);
                        //On notifie à l'adaptater qu'un item a été supprimé
                        adapter.notifyItemRemoved(position);
                        //On synchronise les positions dans l'adaptateur à celle de la liste itemOrders
                        adapter.notifyItemRangeChanged(position, items.size());
                        myIntent = new Intent(OnHoldActivity.this, OnHoldActivity.class);
                        startActivity(myIntent);
                        finish();
                    });
        }
    }

    @Override
    public void onDeclineClick(int position) {
        if (position >= 0 && position < items.size()) {
            MissionDTO mission = items.get(position);
            DocumentReference docRef = db.collection("mission").document(mission.getId());
            //On supprime la mission et on doit notifier à l'admin que la mission a été refusée
            batch.delete(docRef);

            //On récupère la liste des livraisons
            for (DeliveryDTO delivery : deliveryList){
                if(delivery.getTimestamp().toString().equals(mission.getDate().toString())) {
                    DocumentReference deliveryDocRef = db.collection("delivery").document(delivery.getRef());
                    Log.d("delivery Id", delivery.getRef());
                    batch.update(deliveryDocRef, "isAccepted", false);
                    batch.update(deliveryDocRef, "isAttributed", false);
                    batch.update(deliveryDocRef, "delivererEmail", "");
                }
            }
            batch.commit().addOnCompleteListener(
                    tasks->{
                        Log.d("delivery","Mission Refusée!", tasks.getException());
                        Toast.makeText(this, "Mission Refusée! ", Toast.LENGTH_SHORT).show();
                        // On retire la mission de la liste en attente
                        items.remove(position);
                        //On notifie à l'adaptater qu'un item a été supprimé
                        adapter.notifyItemRemoved(position);
                        //On synchronise les positions dans l'adaptateur à celle de la liste itemOrders
                        adapter.notifyItemRangeChanged(position, items.size());
                        myIntent = new Intent(OnHoldActivity.this, OnHoldActivity.class);
                        startActivity(myIntent);
                        finish();
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemOnHold){
            myIntent = new Intent(OnHoldActivity.this, OnHoldActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemInProgress){
            myIntent = new Intent(OnHoldActivity.this, InProgressActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique){
            myIntent = new Intent(OnHoldActivity.this, DriverHistoryActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(OnHoldActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}