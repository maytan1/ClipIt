/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpsolution.clipit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

/**
 *
 * @author maytan
 */
public class AdvancedOptions extends AppCompatActivity {

    private String selectedImagePath;
    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_advanced);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_advanced);
        setSupportActionBar(toolbar);
        
        if(icicle == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                selectedImagePath = null;
            } else {
                selectedImagePath = extras.getString("VIDEO_PATH");
            }
        } else {
            selectedImagePath = (String) icicle.getSerializable("VIDEO_PATH");
        }
    }
    
    public void onAdvancedOptionsSubmit(View view) {
        String options = ((EditText) findViewById(R.id.edOptions)).getText().toString();
        String filename = ((EditText) findViewById(R.id.edFileName)).getText().toString();
        options = options.replace(' ', ',');     
      
        Intent output = new Intent();
        output.putExtra("VIDEO_PATH", selectedImagePath);
        output.putExtra("OPTIONS", options);
        output.putExtra("FILENAME", filename);
        setResult(Activity.RESULT_OK, output);
        finish();
    }
    
}
