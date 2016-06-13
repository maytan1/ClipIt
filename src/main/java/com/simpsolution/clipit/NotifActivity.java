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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;

/**
 *
 * @author maytan
 */
public class NotifActivity extends Activity {

    private String previewPath;
    private String mime;
    private Uri fileUri;
    private VideoView videoPlayer;
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
        
        File f = new File(previewPath);
        fileUri = Uri.fromFile(f);
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(
                        fileUri.toString()
                ));
        
        videoPlayer = (VideoView) findViewById(R.id.videoPlayer);
        videoPlayer.setVideoURI(fileUri);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPlayer);
        videoPlayer.setMediaController(mediaController);
        
    }
    
    
    public void openFile(View view){
        Intent i = new Intent(Intent.ACTION_VIEW);        
        i.setDataAndType(fileUri, mime);
        startActivity(i);
    }
    
    public void shareFile(View view){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType(mime);
        startActivity(Intent.createChooser(shareIntent, "Send"));
    }
    
    @Override
    public void onDestroy() {
        videoPlayer.stopPlayback();
        super.onDestroy();
    }
    
}
