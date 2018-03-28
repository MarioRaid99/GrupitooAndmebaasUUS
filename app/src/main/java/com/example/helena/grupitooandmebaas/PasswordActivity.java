package com.example.helena.grupitooandmebaas;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends AppCompatActivity {

    EditText salasõnaEpost;
    Button lähtestaParool;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        salasõnaEpost = findViewById(R.id.epost);
        lähtestaParool = findViewById(R.id.lähtesta);
        firebaseAuth = firebaseAuth.getInstance();

        lähtestaParool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kasutajaEpost = salasõnaEpost.getText().toString().trim();
                if (kasutajaEpost.equals("")){
                    teade("Palun sisestage oma eposti aadress!");
                }
                else {
                    firebaseAuth.sendPasswordResetEmail(kasutajaEpost).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                teade("Salasõna saadeti emaili!");
                                finish();
                                startActivity(new Intent(PasswordActivity.this, LoginActivity.class));
                            }
                            else {
                                teade("Emaili saatmine ebaõnnestus!");
                            }
                        }
                    });
                }
            }
        });
    }

    public void teade(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
