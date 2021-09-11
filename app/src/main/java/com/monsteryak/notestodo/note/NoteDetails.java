package com.monsteryak.notestodo.note;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.monsteryak.notestodo.R;

public class NoteDetails extends AppCompatActivity {

    private static String alphaNumeric = "Q~a`E!o2R@k:BqZ+lcs]v{85#gM,i (TuX0wN7r-LxJ9eIyO1d*A=m3F$b^H&p}D;n4W[h%Y<t_S.f6G'V)K>j/PUz?C|";
    private static String alphabets = "Q~a`E!o2R@k:BqZ+lcs]v{85#gM,i (TuX0wN7r-LxJ9eIyO1d*A=m3F$b^H&p}D;n4W[h%Y<t_S.f6G'V)K>j/PUz?C|";
    //total length is 92
    //private static String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZab^ *cdefghijklmnopqrstuvwxyz01234567(89).!@#$&`~%-_=+{}[];:'<>,?/|";
    String userId,encryptedContent;
    Intent data;
    FirebaseAuth fAuth;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = getIntent();
        fAuth = FirebaseAuth.getInstance();

        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(decryptCode(data.getStringExtra("content")));
        //content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));
        content.setBackgroundColor(getResources().getColor(data.getIntExtra("codeColor",0),null));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(),EditNote.class);
                intent.putExtra("title",data.getStringExtra("title"));
                intent.putExtra("content",decryptCode(data.getStringExtra("content")));
                intent.putExtra("noteId",data.getStringExtra("noteId"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private String decryptCode(String stringDecrypt) {
        int key = 0;
        char[] encryptedText = new char[stringDecrypt.length()];
        userId = fAuth.getCurrentUser().getUid();

        for (int i = 0; i < userId.length(); i++) {
            char keyChar = userId.charAt(i);
            int keyAscii = (int) keyChar;
            key = key + keyAscii;
        }

        for (int i = 0; i < stringDecrypt.length(); i++) {
            char decChar = stringDecrypt.charAt(i);
            int charIndex = alphabets.indexOf(decChar); //27
            charIndex = charIndex + alphabets.length();  //118
            charIndex = charIndex - (key % alphabets.length());  //38
            charIndex = charIndex % alphaNumeric.length();
            //Toast.makeText(getContext(),"Logout Successfully",Toast.LENGTH_SHORT).show();
            //c = c % alphaNumeric.length();
            char manish = alphaNumeric.charAt(charIndex);
            if (manish == '|') {
                encryptedText[i] = '\n';
            }
            else {
                encryptedText[i] = alphaNumeric.charAt(charIndex);
            }

            //encryptedText[i] = alphaNumeric.charAt(charIndex);
        }

        stringDecrypt = String.valueOf(encryptedText);

        return stringDecrypt;
    }

}