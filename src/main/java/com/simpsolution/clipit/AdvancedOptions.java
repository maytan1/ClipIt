/*
 * The MIT License
 *
 * Copyright 2016 SimpSolutions.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
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
