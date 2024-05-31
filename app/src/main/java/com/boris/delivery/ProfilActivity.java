package com.boris.delivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.boris.delivery.adminActivities.MenuPlanificateurActivity;
import com.boris.delivery.clientActivities.MenuClientActivity;
import com.boris.delivery.driverActivities.OnHoldActivity;
import com.boris.delivery.dto.UserDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class ProfilActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private WriteBatch batch;
    private Button btn;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText emailEditText;
    private EditText mdpEditText;
    private EditText mdpEditText2;
    private EditText phoneEditText;
    private EditText immatriculationEditText;
    private EditText roleEditText;
    private TextView labelImmatriculation;
    private UserDTO user;
    private FirebaseUser firebaseUser;
    private Intent myIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        mAuth = FirebaseAuth.getInstance();
        batch = db.batch();
        firebaseUser = mAuth.getCurrentUser();
        //On récupère l'instance du bouton
        btn = findViewById(R.id.buttonModification);
        //On associe une instance de OnClickListener au bouton
        btn.setOnClickListener((View.OnClickListener) this);
        emailEditText = findViewById(R.id.editTextEmailAddress);
        mdpEditText = findViewById(R.id.editTextPassword);
        mdpEditText2 = findViewById(R.id.editTextPassword2);
        phoneEditText = findViewById(R.id.editTextPhone);
        immatriculationEditText = findViewById(R.id.editTextImmatriculation);
        immatriculationEditText.setVisibility(View.INVISIBLE);
        labelImmatriculation = findViewById(R.id.labelImmatriculation);
        labelImmatriculation.setVisibility(View.INVISIBLE);
        roleEditText = findViewById(R.id.editTextRole);

        CollectionReference utilisateursRef = db.collection("utilisateurs");
        Query query =utilisateursRef.whereEqualTo("email",firebaseUser.getEmail());
        query.get().addOnCompleteListener(task->{
            if(task.isSuccessful()){
                user = new UserDTO();
                for(QueryDocumentSnapshot document : task.getResult()){
                    user.setEmail(document.getString("email"));
                    user.setTelephone(document.getString("telephone"));
                    user.setImmatriculation(document.getString("immatriculation"));
                    user.setRole(document.getLong("role"));
                    user.setId(document.getId());
                    Log.d("Profil", user.toString());
                }
                emailEditText.setText(user.getEmail());
                phoneEditText.setText(user.getTelephone());
                if(user.getRole() == 0){
                    roleEditText.setText("client");
                } else if (user.getRole() == 10) {
                    roleEditText.setText("planificateur");
                } else if (user.getRole() == 20) {
                    roleEditText.setText("chauffeur");
                    immatriculationEditText.setVisibility(View.VISIBLE);
                    labelImmatriculation.setVisibility(View.VISIBLE);
                }
                immatriculationEditText.setText(user.getImmatriculation());
            }else{
                Log.e("Profil", "Erreur : ", task.getException());
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == btn){
            //On récupère les informations du champ du formulaire
            String email = emailEditText.getText().toString();
            String mdp = mdpEditText.getText().toString();
            String mdp2 = mdpEditText2.getText().toString();
            String phone = phoneEditText.getText().toString();
            String immatriculation = immatriculationEditText.getText().toString();

            if(phone.isEmpty() ||(user.getRole() == 20 && immatriculation.isEmpty())){
                Toast.makeText(this, "Veuillez remplir tous les champs avec *", Toast.LENGTH_LONG).show();
            }else {
                user.setTelephone(phone);
                user.setImmatriculation(immatriculation);
                DocumentReference docRefUpdate = db.collection("utilisateurs")
                        .document(user.getId());
                batch.update(docRefUpdate, "telephone", user.getTelephone());
                batch.update(docRefUpdate, "immatriculation", user.getImmatriculation());
                if(!mdp.isEmpty() && !mdp2.isEmpty()){
                    if (mdp.equals(mdp2)){
                        batch.commit().addOnCompleteListener(
                                tasks -> {
                                    firebaseUser.updatePassword(mdp);
                                    Toast.makeText(ProfilActivity.this, "Profil mis à jours !",
                                            Toast.LENGTH_LONG).show();
                                    if (user.getRole() == 0) {//indique que l'utilisateur est un client
                                        myIntent = new Intent(ProfilActivity.this, MenuClientActivity.class);
                                    } else if (user.getRole() == 10) {//indique que l'utilisateur est un planificateur
                                        myIntent = new Intent(ProfilActivity.this, MenuPlanificateurActivity.class);
                                    } else if (user.getRole() == 20) {//indique que l'utilisateur est un chauffeur
                                        myIntent = new Intent(ProfilActivity.this, OnHoldActivity.class);
                                    }
                                    startActivity(myIntent);
                                    finish();
                                });
                    }else{
                        Toast.makeText(this, "mots de passes différents !", Toast.LENGTH_LONG).show();
                    }
                } else if(mdp.isEmpty() && mdp2.isEmpty()) {
                        batch.commit().addOnCompleteListener(
                                tasks -> {
                                    Toast.makeText(ProfilActivity.this, "Profil mis à jours !",
                                            Toast.LENGTH_LONG).show();
                                    if (user.getRole() == 0) {//indique que l'utilisateur est un client
                                        myIntent = new Intent(ProfilActivity.this, MenuClientActivity.class);
                                    } else if (user.getRole() == 10) {//indique que l'utilisateur est un planificateur
                                        myIntent = new Intent(ProfilActivity.this, MenuPlanificateurActivity.class);
                                    } else if (user.getRole() == 20) {//indique que l'utilisateur est un chauffeur
                                        myIntent = new Intent(ProfilActivity.this, OnHoldActivity.class);
                                    }
                                    startActivity(myIntent);
                                    finish();
                                });
                }else{
                    Toast.makeText(this, "mots de passes différents !", Toast.LENGTH_LONG).show();
                }

            }
        }
    }
}