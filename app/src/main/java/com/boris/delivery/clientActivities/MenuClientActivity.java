package com.boris.delivery.clientActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.adapter.BooksAdapter;
import com.boris.delivery.deliveryInterface.OnItemClickListener;
import com.boris.delivery.dto.BookDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuClientActivity extends AppCompatActivity implements OnItemClickListener {
    private FirebaseAuth mAuth;
    RecyclerView recyclerViewBooks ;
    List<BookDTO> items;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email;
    Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_client);
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();

        CollectionReference booksCollection = db.collection("book");
        items = new ArrayList<>();
       booksCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                BookDTO book = new BookDTO();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getLong("image")==1){
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.book1,document.getDouble("rating"), document.getDouble("price"));
                    } else if (document.getLong("image")==2) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.book2,document.getDouble("rating"), document.getDouble("price"));
                    }else if (document.getLong("image")==3){
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.book3,document.getDouble("rating"), document.getDouble("price"));
                    }else if (document.getLong("image")==4){
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.book4,document.getDouble("rating"), document.getDouble("price"));
                    }else if (document.getLong("image")==5){
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.book5,document.getDouble("rating"), document.getDouble("price"));
                    }else{
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"),document.getString("author"), R.drawable.delivery_logo,document.getDouble("rating"), document.getDouble("price"));
                    }
                    items.add(book);
                }
                recyclerViewBooks = findViewById(R.id.recyclerViewBook);
                recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
                BooksAdapter adapter = new BooksAdapter(items, (OnItemClickListener) this);
                recyclerViewBooks.setAdapter(adapter);

            }
        });


    }

    @Override
    public void onItemClick(int position) {
        //On récupère le produit sur lequel on a cliqué
        BookDTO clickedItem = items.get(position);
        //On vérifie que le produit n'est pas déjà présent dans le panier de cette utilisateur
        CollectionReference bookRef = db.collection("order");
        Query query =bookRef.whereEqualTo("idBook",clickedItem.getId())
                .whereEqualTo("emailUser", email).whereEqualTo("inCart", true);
        query.get().addOnCompleteListener(task->{
            if(task.isSuccessful()){
                if(task.getResult().isEmpty()){
                    //on crée une instance de commande
                    Map<String, Object> newOrder = new HashMap<>();
                    newOrder.put("emailUser", email);
                    newOrder.put("idBook", clickedItem.getId());
                    newOrder.put("quantity", 1);
                    newOrder.put("inCart", true);
                    newOrder.put("inDelivery", false);
                    //on ajoute la commande au panier
                    db.collection("order").document().set(newOrder).addOnSuccessListener(
                                    unused-> Toast.makeText(this, "Produit ajouté au panier!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e-> Log.e("delivery", e.toString()));
                }else{
                    Toast.makeText(this, "livre présent dans le panier !",Toast.LENGTH_SHORT).show();
                    Log.e("delivery", "Erreur : livre présent dans le panier", task.getException());
                }
            }else{
                Log.e("delivery", "Erreur lors de la vérification du panier", task.getException());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.itemCart){
            myIntent = new Intent(MenuClientActivity.this, PanierActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemHome) {
            myIntent = new Intent(MenuClientActivity.this, MenuClientActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique) {
            myIntent = new Intent(MenuClientActivity.this, ClientHistoryActivity.class);
            startActivity(myIntent);
            finish();
        } else if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(MenuClientActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}