package com.example.helena.grupitooandmebaas;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends AppCompatActivity {

    EditText eesNimi, perekonnaNimi, epost, salasõna;
    Button btn_registreeri, btn_pilt;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    String _eesNimi, _perekonnaNimi, _epost, _salasõna;
    ImageView profiiliPilt;
    private StorageReference storageReference;
    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profiiliPilt = findViewById(R.id.profile_pic);
        btn_pilt = findViewById(R.id.btnlisa);
        eesNimi = findViewById(R.id.eesNimi);
        perekonnaNimi = findViewById(R.id.pereNimi);
        epost = findViewById(R.id.epost);
        salasõna = findViewById(R.id.password);
        btn_registreeri = findViewById(R.id.btnRegister);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        btn_pilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pilt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(pilt,CAMERA_REQUEST_CODE);
            }
        });

        btn_registreeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valideeri()) {

                    String k_epost = epost.getText().toString().trim();
                    String k_salasõna = salasõna.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(k_epost, k_salasõna)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        progressDialog.setMessage("Andmete edastamisega läheb aega, palun kannatust!");
                                        progressDialog.show();
                                        pildiUpload();
                                        saadaEpostiKinnitus();
                                    }

                                    else {

                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Bitmap bitmap =(Bitmap)data.getExtras().get("data");
            profiiliPilt.setImageBitmap(bitmap);
        }
    }

    private void pildiUpload(){
        progressDialog.setMessage("Pildi üleslaadimine");
        progressDialog.show();
        StorageReference pathReference = storageReference.child("pildid/profiiliPilt.jpg");
        profiiliPilt.setDrawingCacheEnabled(true);
        profiiliPilt.buildDrawingCache();
        Bitmap bitmap = profiiliPilt.getDrawingCache();
        ByteArrayOutputStream baitOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baitOS);
        byte[] data = baitOS.toByteArray();
        UploadTask uploadTask = pathReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata();
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Picasso.get().load(downloadUrl).fit().centerCrop().into(profiiliPilt);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                teade("Pilti ei laetud ülesse");
            }
        });
    }


    private boolean valideeri() {

        boolean tulemus = false;

        _eesNimi = eesNimi.getText().toString();
        _perekonnaNimi = perekonnaNimi.getText().toString();
        _epost = epost.getText().toString();
        _salasõna = salasõna.getText().toString();

        if (_eesNimi.isEmpty() || _perekonnaNimi.isEmpty() || _epost.isEmpty() || _salasõna.isEmpty()) {
            teade("Täida kõik väljad!");
        }

        else {
            tulemus = true;
        }

        return tulemus;
    }


    private void saadaEpostiKinnitus() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        saadaKasutajaAndmed();
                        teade("Registreerimine õnnestus, teile saadeti kinnitus email!");
                        finish();
                        firebaseAuth.signOut();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        teade("Kinnitus emaili ei saadetud!");
                    }
                }
            });
        }
    }


    private void saadaKasutajaAndmed() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        UserProfileData userProfileData = new UserProfileData(_eesNimi, _perekonnaNimi, _epost);
        databaseReference.setValue(userProfileData);
    }


    public void teade(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}