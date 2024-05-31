package com.boris.delivery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn;
    private Button btnInscription;
    private Button btnInformations;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //On récupère l'instance du bouton
        btn = findViewById(R.id.buttonConnexion);
        //On associe une instance de OnClickListener au bouton
        btn.setOnClickListener(this);

        //On récupère l'instance du bouton
        btnInscription = findViewById(R.id.buttonInscription);
        //On associe une instance de OnClickListener au bouton
        btnInscription.setOnClickListener(this);

        //On récupère l'instance du bouton
        btnInformations = findViewById(R.id.buttonInformations);
        //On associe une instance de OnClickListener au bouton
        btnInformations.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        //On vérifie quellle bouton a été cliqué
        if (v == btn) {
            Intent myIntent = new Intent(MainActivity.this, ConnexionActivity.class);
            startActivity(myIntent);
        } else if (v == btnInscription) {
            Intent myIntent = new Intent(MainActivity.this, InscriptionActivity.class);
            startActivity(myIntent);
        } else if (v == btnInformations) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_LOCATION_PERMISSION);
            }else {
                Toast.makeText(this, "Contactez le Bingo : 0754577867", Toast.LENGTH_LONG)
                        .show();
            }

        }
    }

}