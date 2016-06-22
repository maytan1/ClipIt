/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpsolution.clipit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maytan
 */
public class Feedback extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    public void submitFeedback(View view) {
        String name = ((EditText)findViewById(R.id.username)).getText().toString();
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String feedback = ((EditText)findViewById(R.id.feedback)).getText().toString();
        
        if(name.length()>0 && email.length()>0 && feedback.length()>0){
            try {
                String data = URLEncoder.encode("Name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                data += "&" + URLEncoder.encode("Email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                data += "&" + URLEncoder.encode("Feedback", "UTF-8") + "=" + URLEncoder.encode(feedback, "UTF-8");
                URL url = new URL("http://localhost/app.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter o = new OutputStreamWriter(conn.getOutputStream());
                o.write(data);
                o.flush(); 
                Toast.makeText(this, "Feedback sent successfully.", Toast.LENGTH_SHORT).show();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Feedback.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Feedback.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
