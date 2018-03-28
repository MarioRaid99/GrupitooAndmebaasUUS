package com.example.helena.grupitooandmebaas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    TextView profiil_eesnimi, profiil_perekonnanimi, profiil_epost;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
            case  R.id.välja:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.kasutaja:
                finish();
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profiil_eesnimi= findViewById(R.id.profileEesnimi);
        profiil_perekonnanimi = findViewById(R.id.profilePerenimi);
        profiil_epost = findViewById(R.id.profileEpost);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
//saame firebase userid andmed kätte
        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getCurrentUser().getUid());


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserProfileData userProfileData = dataSnapshot.getValue(UserProfileData.class);
                profiil_eesnimi.setText(userProfileData.getEesNimi());
                profiil_perekonnanimi.setText(userProfileData.getPereNimi());
                profiil_epost.setText(userProfileData.getEpost());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Kirjutab konkreetse errori.
                Toast.makeText(UserProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();

            }
        });


    }
}

