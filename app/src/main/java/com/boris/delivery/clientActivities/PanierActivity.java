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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boris.delivery.ProfilActivity;
import com.boris.delivery.R;
import com.boris.delivery.adapter.OrdersAdapter;
import com.boris.delivery.deliveryInterface.OnButtonClickListener;
import com.boris.delivery.dto.BookDTO;
import com.boris.delivery.dto.OrderDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PanierActivity extends AppCompatActivity implements OnButtonClickListener,View.OnClickListener {

    private FirebaseAuth mAuth;
    RecyclerView recyclerViewBasket ;
    List<BookDTO> itemBooks;
    List<OrderDTO> itemOrders;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    WriteBatch batch;
    OrdersAdapter adapter;
    String email;
    Double total;
    Button btnValider;
    DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);
        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();
        total= 0.0;
        btnValider = findViewById(R.id.validerPanier);
        btnValider.setOnClickListener(this);
        //on utilise le writeBatch pour faire des transactions par lot
        batch = db.batch();
        //on récupère nos livres
        CollectionReference booksCollection = db.collection("book");
        itemBooks = new ArrayList<>();
        //On crée un format décimal pour afficher le total
        decimalFormat = new DecimalFormat("#.##");
        booksCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                BookDTO book = new BookDTO();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getLong("image") == 1) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.book1, document.getDouble("rating"), document.getDouble("price"));
                    } else if (document.getLong("image") == 2) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.book2, document.getDouble("rating"), document.getDouble("price"));
                    } else if (document.getLong("image") == 3) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.book3, document.getDouble("rating"), document.getDouble("price"));
                    } else if (document.getLong("image") == 4) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.book4, document.getDouble("rating"), document.getDouble("price"));
                    } else if (document.getLong("image") == 5) {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.book5, document.getDouble("rating"), document.getDouble("price"));
                    } else {
                        book = new BookDTO(document.getId(), document.getString("title"), document.getString("summary"), document.getString("author"), R.drawable.delivery_logo, document.getDouble("rating"), document.getDouble("price"));
                    }
                    itemBooks.add(book);
                }
            }
        });

        //On récupère la liste des commandes dans le pannier de l'utilisateur
        CollectionReference ordersCollection = db.collection("order");
        itemOrders = new ArrayList<>();
        ordersCollection.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        OrderDTO order = new OrderDTO();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(document.getString("emailUser").equals(email) && document.getBoolean("inCart") && !document.getBoolean("inDelivery")){
                                order = new OrderDTO(document.getId(), document.getString("email"), document.getString("idBook"), document.getBoolean("inBasket"), document.getBoolean("inDelivery"), document.getDouble("quantity"));
                                itemOrders.add(order);
                            }

                        }
                        recyclerViewBasket = findViewById(R.id.recyclerViewBasket);
                        recyclerViewBasket.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new OrdersAdapter(itemOrders, itemBooks, (OnButtonClickListener) this);
                        recyclerViewBasket.setAdapter(adapter);
                        //On souhaite éditer le total des prix du panier
                        for(OrderDTO item : itemOrders){
                            //On récupère le livre associé à l'ordre
                            BookDTO associedBook = this.getAssociedBook(item.getIdBook());
                            //On rédui la variable total du prix de l'ordre
                            total += associedBook.getPrice()*item.getQuantity();
                            Log.d("delivery", String.valueOf(total));
                            Log.d("price", String.valueOf(item.getQuantity()));
                            //On édite le total des prix du panier
                        }
                        EditText TotalEditTex = findViewById(R.id.editTextTotal);
                        TotalEditTex.setText(decimalFormat.format(total)+" €");
                    }else{
                        Log.e("Delivery-Panier", "Erreur : ", task.getException());
                    }
                });
    }

    public BookDTO getAssociedBook(String idBook){
        BookDTO book =new BookDTO();
        for (BookDTO item : itemBooks) {
            if (item.getId().equals(idBook)) {
                book = item;
            }
        }
        return book;
    }
    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < itemOrders.size()) {
            OrderDTO orderToDelete = itemOrders.get(position);
            // On supprime le document dans Firestore en utilisant son id
            db.collection("order").document(orderToDelete.getId()).delete()
                    .addOnSuccessListener(unused -> {
                        // La suppression a réussi
                        Toast.makeText(this, "élément supprimé du panier! " + position, Toast.LENGTH_SHORT).show();
                        // Supprimez également l'élément de la liste locale si nécessaire
                        itemOrders.remove(position);
                        //On notifie à l'adaptater qu'un item a été supprimé
                        adapter.notifyItemRemoved(position);
                        //On synchronise les positions dans l'adaptateur à celle de la liste itemOrders
                        adapter.notifyItemRangeChanged(position, itemOrders.size());
                        //On récupère le livre associé à l'ordre
                        BookDTO associedBook = adapter.getAssociedBook(orderToDelete.getIdBook());
                        //On rédui la variable total du prix de l'ordre
                        if (total !=0.0) {
                            total -= associedBook.getPrice()*orderToDelete.getQuantity();
                        }
                        if(total<0.0){
                            total = 0.0;
                        }
                        //On édite le total des prix du panier
                        EditText TotalEditTex = findViewById(R.id.editTextTotal);
                        TotalEditTex.setText(decimalFormat.format(total)+" €");
                    })
                    .addOnFailureListener(e -> {
                        // La suppression a échoué
                        Log.e("Firestore", "Erreur lors de la suppression de la commande : " + e.getMessage() + position);
                        Toast.makeText(this, "Erreur lors de la suppression!"+ position, Toast.LENGTH_SHORT).show();
                    });

        }else{
            Toast.makeText(this, "position invalide", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onIncreaseClick(int position) {
        if (position >= 0 && position < itemOrders.size()) {
            OrderDTO orderToIncrease = itemOrders.get(position);
            //On modifie la quantité de l'ordre
            orderToIncrease.setQuantity(orderToIncrease.getQuantity()+1);
            //On notifie le changement à l'adaptater
            adapter.notifyDataSetChanged();
            //On récupère le livre associé à l'ordre
            BookDTO associedBook = adapter.getAssociedBook(orderToIncrease.getIdBook());
            //On incrémente la variable total du prix du livre
            total += associedBook.getPrice();
            //On  édite le total des prix du panier
            EditText TotalEditTex = findViewById(R.id.editTextTotal);
            TotalEditTex.setText(decimalFormat.format(total)+" €");
        }
    }

    @Override
    public void onDecreaseClick(int position) {
        if (position >= 0 && position < itemOrders.size()) {
            OrderDTO orderToDecrease = itemOrders.get(position);
            //pas de quantité négative
            if(orderToDecrease.getQuantity()>0){
                //On modifie la quantité de l'ordre
                orderToDecrease.setQuantity(orderToDecrease.getQuantity()-1);
                //On notifie le changement à l'adaptater
                adapter.notifyDataSetChanged();
                //On récupère le livre associé à l'ordre
                BookDTO associedBook = adapter.getAssociedBook(orderToDecrease.getIdBook());
                //On décrémente la variable total du prix du livre
                if (total != 0.0) {
                    total -= associedBook.getPrice();
                }
                if(total<0.0){
                    total = 0.0;
                }
                //On édite le total des prix du panier
                EditText TotalEditTex = findViewById(R.id.editTextTotal);
                TotalEditTex.setText(decimalFormat.format(total)+" €");
            }else{
                Toast.makeText(this,"Pas de quantité négative!",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onValidateClick(int position) {

    }

    @Override
    public void onDeclineClick(int position) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.second_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent myIntent;
        if (item.getItemId() ==R.id.itemProfil) {
            myIntent = new Intent(PanierActivity.this, ProfilActivity.class);
            startActivity(myIntent);
            //finish();
        }else if (item.getItemId() ==R.id.itemLogout) {
            mAuth.signOut();
            finish();
        }else if (item.getItemId() ==R.id.itemHome) {
            myIntent = new Intent(PanierActivity.this, MenuClientActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() ==R.id.itemHistorique) {
            myIntent = new Intent(PanierActivity.this, ClientHistoryActivity.class);
            startActivity(myIntent);
            finish();
        }else if (item.getItemId() == android.R.id.home) {
            //on met à jour les datas du panier
            for(OrderDTO order : itemOrders) {
                DocumentReference docRef = db.collection("order").document(order.getId());
                batch.update(docRef, "quantity", order.getQuantity());
            }
            batch.commit().addOnCompleteListener(
                    tasks->Log.d("delivery","panier mis à jours", tasks.getException())
            );
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v){
        if (v == btnValider) {
            if(total > 0.0){
            //on met à jour les datas du panier
            for(OrderDTO order : itemOrders) {
                DocumentReference docRef = db.collection("order").document(order.getId());
                batch.update(docRef, "quantity", order.getQuantity());
            }
            batch.commit().addOnCompleteListener(
                    tasks->Log.d("delivery","panier mis à jours", tasks.getException())
            );
            Intent cartIntent = new Intent(PanierActivity.this, MapActivity.class);
            cartIntent.putExtra("total", total);
            cartIntent.putExtra("orderList", (Serializable) itemOrders);
            startActivity(cartIntent);
            finish();

            }else{
                Toast.makeText(this, "Panier Vide", Toast.LENGTH_SHORT).show();
            }
        }
    }
}