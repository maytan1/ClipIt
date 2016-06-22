/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpsolution.clipit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;

/**
 *
 * @author maytan
 */
public class NotifActivity extends AppCompatActivity {

    private String previewPath;
    private String mime;
    private Uri fileUri;
    private VideoView videoPlayer;
    private MediaController mediaController;
    /**
     * Called when the activity is first created.
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_notif);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
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
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPlayer);
        videoPlayer.setMediaController(mediaController);
        
    }
    
    public void playContent(View view) {
        videoPlayer.start();
        ((FrameLayout) findViewById(R.id.videoFrame)).setForeground(null);
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
    
    public void spread(View view) {
        final CharSequence socials[] = new CharSequence[]{"WhatsApp", "Facebook", "Twitter"};
        AlertDialog.Builder choice = new AlertDialog.Builder(this);
        choice.setTitle("Choose how..");
        choice.setItems(socials, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int i) {
                if("WhatsApp".equals(socials[i])){
                    ShareUtils.shareWhatsapp(NotifActivity.this, "Check out this new app ClipIt. Convert your music videos to songs, convert videos to format playable on phones and much more", "http://www.google.com");
                }
                else if("Facebook".equals(socials[i])){
                    ShareUtils.shareFacebook(NotifActivity.this, "Check out this new app ClipIt. Convert your music videos to songs, convert videos to format playable on phones and much more", "http://www.google.com");
                }
                else if("Twitter".equals(socials[i])){
                    ShareUtils.shareTwitter(NotifActivity.this, "Check out this new app ClipIt. Convert your music videos to songs, convert videos to format playable on phones and much more.", "http://www.google.com", "simpsolutions", "ClipIt,videoconversion,ffmpeg,android");
                }
            }
        });
        choice.show();
    }
    
    @Override
    public void onDestroy() {
        videoPlayer.stopPlayback();
        super.onDestroy();
    }
    
}
