/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpsolution.clipit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import java.io.File;

/**
 *
 * @author maytan
 */
public class NotifActivity extends Activity {

    private String previewPath;
    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_notification);
        // ToDo add your GUI initialization code here  
        
        if(icicle == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                previewPath = "No preview";
            } else {
                previewPath = extras.getString("PREVIEW_FILE");
            }
        } else {
            previewPath = (String) icicle.getSerializable("PREVIEW_FILE");
        }
        
        Log.d("clipit", previewPath);
        
    }
    
    public void openFile(View view){
        Intent i = new Intent(Intent.ACTION_VIEW);
        File f = new File(previewPath);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(
                        Uri.fromFile(f).toString()
                ));
        i.setDataAndType(Uri.fromFile(f), mime);
        startActivity(i);
    }
    
}
