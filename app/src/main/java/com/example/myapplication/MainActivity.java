package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addWorkBtn;
    RecyclerView recyclerView;
    ImageButton menuBtn;
    WorkAdapter workAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addWorkBtn = findViewById(R.id.add_work_btn);
        recyclerView = findViewById(R.id.recyler_view);
        menuBtn = findViewById(R.id.menu_btn);

        addWorkBtn.setOnClickListener((v)-> startActivity(new Intent(MainActivity.this,WorkDetailsActivity.class)) );
        menuBtn.setOnClickListener((v)->showMenu() );
        setupRecyclerView();
    }

    void showMenu(){
        PopupMenu popupMenu  = new PopupMenu(MainActivity.this,menuBtn);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle()=="Logout"){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

    }

    void setupRecyclerView(){
        Query query  = Utility.getCollectionReferenceForWorks().orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Work> options = new FirestoreRecyclerOptions.Builder<Work>()
                .setQuery(query,Work.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        workAdapter = new WorkAdapter(options,this);
        recyclerView.setAdapter(workAdapter);

        // Thiết lập ItemTouchHelper cho RecyclerView
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(workAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        workAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        workAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        workAdapter.notifyDataSetChanged();
    }
}