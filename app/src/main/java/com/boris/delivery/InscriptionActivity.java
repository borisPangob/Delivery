package com.boris.delivery;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class InscriptionActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;
    private Button btn;
    private Spinner sp;
    private int role = -1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText emailEditText;
    private EditText mdpEditText;
    private EditText mdpEditText2;
    private EditText phoneEditText;
    private EditText immatriculationEditText;
    private TextView labelImmatriculation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);
        mAuth = FirebaseAuth.getInstance();
        //On Récupère l'instance du bouton
        btn = findViewById(R.id.buttonInscription);
        //On associe une instance de OnClickListener au bouton
        btn.setOnClickListener(this);
        emailEditText = findViewById(R.id.editTextEmailAddress);
        mdpEditText = findViewById(R.id.editTextPassword);
        mdpEditText2 = findViewById(R.id.editTextPassword2);
        phoneEditText = findViewById(R.id.editTextPhone);
        immatriculationEditText = findViewById(R.id.editTextImmatriculation);
        immatriculationEditText.setVisibility(View.INVISIBLE);
        labelImmatriculation = findViewById(R.id.labelImmatriculation);
        labelImmatriculation.setVisibility(View.INVISIBLE);
        sp = findViewById(R.id.choixRole);

        sp.setOnItemSelectedListener(this);
        //On crée un arrayAdapter qui utilise un tableau de String et un spinner layout par défaut
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Choix, android.R.layout.simple_spinner_item);
        //On spécifie le layout qui sera utilisé quand la liste des choix apparait
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //On applique l'adaptateur au spinner
        sp.setAdapter(adapter);

    }
    @Override
    public void onClick(View v) {
        //Si le bouton inscription est cliqué
        if (v == btn) {
            //On récupère les informations du champ du formulaire
            String email = emailEditText.getText().toString();
            String mdp = mdpEditText.getText().toString();
            String mdp2 = mdpEditText2.getText().toString();
            String phone = phoneEditText.getText().toString();
            String immatriculation = immatriculationEditText.getText().toString();

            //On crée une représentation de notre objet dans la bdd "utilisateurs"
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("email", email);
            newUser.put("telephone", phone);
            newUser.put("role", role);
            newUser.put("immatriculation", immatriculation);
            //On vérifie que le mot de passe correspond
            if(mdp.isEmpty() || email.isEmpty() || phone.isEmpty() || role == -1 || mdp2.isEmpty()){
                Toast.makeText(this, "Veuillez remplir tous les champs avec *", Toast.LENGTH_LONG).show();
            }else {
                if (mdp.equals(mdp2)) {
                    mAuth.createUserWithEmailAndPassword(email, mdp)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // inscription réussie
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                            if (emailVerificationTask.isSuccessful()) {
                                                // La vérification par e-mail a été envoyée avec succès
                                                // On peut rediriger l'utilisateur vers une page de confirmation
                                                Log.e("Inscription", "envoi de la vérification par e-mail réussie.");
                                            } else {
                                                // Une erreur s'est produite lors de l'envoi de la vérification par e-mail
                                                Log.e("Inscription", "Erreur lors de l'envoi de la vérification par e-mail.");
                                                Log.e("Inscription", emailVerificationTask.getException().toString());
                                            }
                                        });
                                        updateUI(user, newUser);
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        updateUI(null, null);
                                    }
                                }
                            });
                } else {
                    Toast.makeText(this, "Mots de passe différents", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void updateUI(FirebaseUser user, Map<String, Object> newUser) {
        if (user != null) {
            //On ajoute les informations complémentaires à l'utilisateur dans la table "utilisateurs"
            db.collection("utilisateurs").document().set(newUser).addOnSuccessListener(
                            unused-> Toast.makeText(this, "Utilisateur enregistré", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e-> Log.e("delivery", e.toString()));
            //si c'est un chauffeur, on l'inscrit à un topic afin qu'il puisse recevoir les notifications
            if(role == 20){
                FirebaseMessaging.getInstance().subscribeToTopic("nouvelle_mission")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Log.w("notif", "échec souscription");
                                }else{
                                    Log.w("notif", "souscription réussie");
                                }
                            }
                        });
            }
            //Redirection vers la page d'acceuil
            Intent intent = new Intent(InscriptionActivity.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Inscription Reéussie", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Echec inscription", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] tabChoix = getResources().getStringArray(R.array.Choix);
        switch (tabChoix[position]) {
            case "client":
                role = 0; //indique que l'utilisateur est un client
                immatriculationEditText.setVisibility(View.INVISIBLE);
                labelImmatriculation.setVisibility(View.INVISIBLE);
                immatriculationEditText.setText("");

                break;
            case "planificateur":
                role = 10;//indique que l'utilisateur est un planificateur
                immatriculationEditText.setVisibility(View.INVISIBLE);
                labelImmatriculation.setVisibility(View.INVISIBLE);
                immatriculationEditText.setText("");

                break;
            case "chauffeur":
                role = 20;//indique que l'utilisateur est un chauffeur
                immatriculationEditText.setVisibility(View.VISIBLE);
                labelImmatriculation.setVisibility(View.VISIBLE);

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
