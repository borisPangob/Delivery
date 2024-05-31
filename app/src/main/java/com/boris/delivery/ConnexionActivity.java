package com.boris.delivery;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boris.delivery.adminActivities.MenuPlanificateurActivity;
import com.boris.delivery.clientActivities.MenuClientActivity;
import com.boris.delivery.driverActivities.OnHoldActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

public class ConnexionActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    Button btn;
    Button btnInscription;
    Button btnResetPassword;
    Intent myIntent = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText emailEditTex;
    EditText mdpEditTex ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
        mAuth = FirebaseAuth.getInstance();

        emailEditTex = findViewById(R.id.editTextEmailAddress);
        mdpEditTex = findViewById(R.id.editTextPassword);
        //On récupère l'instance du bouton
        btn = findViewById(R.id.buttonConnexion);
        //On associe une instance de OnClickListener au bouton
        btn.setOnClickListener(this);

        //On récupère l'instance du bouton
        btnInscription = findViewById(R.id.buttonInscription);
        //On associe une instance de OnClickListener au bouton
        btnInscription.setOnClickListener(this);

        //On récupère l'instance du bouton
        btnResetPassword = findViewById(R.id.buttonReset);
        //On associe une instance de OnClickListener au bouton
        btnResetPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //On vérifie quellle bouton a été cliqué
        if (v == btn) {
            String email = emailEditTex.getText().toString();
            String mdp = mdpEditTex.getText().toString();

            if(!email.isEmpty() && !mdp.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, mdp)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // connexion réussie
                                    Log.d(TAG, "connexion réussie");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //On vérifie que l'email de l'utilisateur est vérifié
                                    if (user.isEmailVerified()) {
                                        emailEditTex.setText("");
                                        mdpEditTex.setText("");
                                        updateUI(user);
                                    } else {
                                        mdpEditTex.setText("");
                                        Log.w("verification", "email non vérifié" + user.getEmail());
                                        toast();
                                    }
                                } else {
                                    // En cas d'échec de connexion.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    updateUI(null);
                                }
                            }
                        });
            }else{
                Toast.makeText(this, "Veuillez remplir tous les champs!", Toast.LENGTH_LONG).show();
            }

        } else if (v == btnInscription) {
            myIntent = new Intent(ConnexionActivity.this, InscriptionActivity.class);
            startActivity(myIntent);
        } else if (v == btnResetPassword) {
            if(!emailEditTex.getText().toString().isEmpty()) {
                mAuth.sendPasswordResetEmail(emailEditTex.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("verification", "Email envoyé.");
                                }
                            }
                        });
                Toast.makeText(this, "Un email vous a été envoyé", Toast.LENGTH_LONG)
                        .show();
            }else{
                Toast.makeText(this, "Remplissez le champs email", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void toast() {
        Toast.makeText(this, "email non vérifié ", Toast.LENGTH_LONG).show();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //On récupère les données via une requete asynchrone
            CollectionReference utilisateursRef = db.collection("utilisateurs");
            Query query =utilisateursRef.whereEqualTo("email",user.getEmail());
            query.get().addOnCompleteListener(task->{
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        Log.e("Role", document.getId()+"=>"+document.getData());
                        Long role = document.getLong("role");
                        Log.e("Valeur Role", String.valueOf(role));
                        if (role == 0) {//indique que l'utilisateur est un client
                            myIntent = new Intent(ConnexionActivity.this, MenuClientActivity.class);
                        } else if (role == 10) {//indique que l'utilisateur est un planificateur
                            myIntent = new Intent(ConnexionActivity.this, MenuPlanificateurActivity.class);
                        } else if (role == 20) {//indique que l'utilisateur est un chauffeur
                            myIntent = new Intent(ConnexionActivity.this, OnHoldActivity.class);
                        }
                    }
                }else{
                    Log.e("TP-CABANI", "Erreur : ", task.getException());
                }
                if(myIntent != null) {
                    startActivity(myIntent);
                    Toast.makeText(this, "Bienvenu " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(this, "redirection impossible :(", Toast.LENGTH_LONG).show();
                }
             });

        } else {
            Toast.makeText(ConnexionActivity.this, "Echec de l'authentication !",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "identifiants incorrects", Toast.LENGTH_SHORT).show();
        }
    }
}