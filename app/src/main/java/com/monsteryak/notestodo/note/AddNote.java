package com.monsteryak.notestodo.note;

import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.monsteryak.notestodo.R;

import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity {

    FirebaseFirestore fStore;
    EditText contentTitle, noteContent;
    ProgressBar progressBar;
    FirebaseUser fUser;
    FirebaseAuth fAuth;
    String userId;

    private static String alphaNumeric = "Q~a`E!o2R@k:BqZ+lcs]v{85#gM,i (TuX0wN7r-LxJ9eIyO1d*A=m3F$b^H&p}D;n4W[h%Y<t_S.f6G'V)K>j/PUz?C|";
    private static String alphabets = "Q~a`E!o2R@k:BqZ+lcs]v{85#gM,i (TuX0wN7r-LxJ9eIyO1d*A=m3F$b^H&p}D;n4W[h%Y<t_S.f6G'V)K>j/PUz?C|";
    //total length is 92
    //private static String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZab^ *cdefghijklmnopqrstuvwxyz01234567(89).!@#$&`~%-_=+{}[];:'<>,?/|";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        noteContent = findViewById(R.id.addNoteContent);
        contentTitle = findViewById(R.id.addNoteTitle);

        progressBar = findViewById(R.id.progressBar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cTitle = contentTitle.getText().toString();
                String nContent = noteContent.getText().toString();

                if (cTitle.isEmpty() || nContent.isEmpty()) {
                    Toast.makeText(AddNote.this, "One Field is Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                for (int i = 0; i < nContent.length(); i++) {
                    char testChar = nContent.charAt(i);
                    if (testChar == '|') {
                        Toast.makeText(AddNote.this, "Invalid Character |", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                }

                nContent = encryptCode(nContent);

                DocumentReference documentReference = fStore.collection("Users").document(fUser.getUid()).collection("Notes").document();
                Map<String,Object> note = new HashMap<>();
                note.put("title",cTitle);
                note.put("content",nContent);
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AddNote.this, "Note Added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNote.this, "Error, Try Again"+ e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.close) {
            Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private String encryptCode(String stringEncrypt) {
        int key = 0;
        char[] encryptedText = new char[stringEncrypt.length()];
        userId = fAuth.getCurrentUser().getUid();

        for (int i = 0; i < userId.length(); i++) {
            char keyChar = userId.charAt(i);
            int keyAscii = (int) keyChar;
            key = key + keyAscii;
        }

        for (int i = 0; i < stringEncrypt.length(); i++) {
            char encChar = stringEncrypt.charAt(i);
            int charIndex = alphaNumeric.indexOf(encChar);    //38
            charIndex = charIndex + (key % alphabets.length());          //55
            charIndex = charIndex % alphabets.length();         //55
            //Toast.makeText(getContext(),"Hello "+ b,Toast.LENGTH_SHORT).show();
            encryptedText[i] = alphabets.charAt(charIndex);
        }

        stringEncrypt = String.valueOf(encryptedText);

        return stringEncrypt;
    }
}