package com.simpsolution.clipit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.florescu.android.rangeseekbar.RangeSeekBar.OnRangeSeekBarChangeListener;

public class MainActivity extends AppCompatActivity {

    //<editor-fold defaultstate="collapsed" desc="Variables and Objects">
    private ProgressDialog progressDialog;
    private Spinner containers;
    private RadioButton rbAudio, rbVideo;
    private ArrayAdapter<CharSequence> adapter;
    private RangeSeekBar<Long> rsb;
    private SeekBar sb;
    private CheckBox cb;
    private TextView tvStart, tvEnd;
    private NotificationCompat.Builder notif;
    private NotificationManager notifManager;
    private Intent notifyIntent;
    private PendingIntent notifyPendingIntent;
    private final int PICK_VIDEO_REQUEST = 1;
    private final int ADVANCED_OPTIONS_REQUEST = 2;
    private final int FINISH_NOTIFICATION = 001;
    private static final String TAG = "clipit";
    private static int STATE = 0;
    private String selectedImagePath = null;
    private String outPath = null;
    private String options = null;
    private String clip = null;
    private String filename = null;
    private String ext = null;
    private String preview = null;
    private long timeInSecs;
    private int bitrate;
    private int maxBitrate;

    FFmpeg ffmpeg;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Activity State Methods">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Drawing Status Bar Material Design on Supported Versions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //@TargetAPI(21)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        
        //Making App Directory ClipIt
        outPath = Environment.getExternalStorageDirectory()+"/ClipIt";
        File f = new File(outPath);
        if(!(f.exists() && f.isDirectory())){
            f.mkdir();
        }

        init();

        loadFFMpegBinary();

    }
    
    @Override
    protected void onPause() {
        super.onPause();
        STATE = 1;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        STATE = 0;
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="init function">
    public void init(){
        
        //Initializing Advanced Checkbox
        cb = (CheckBox) findViewById(R.id.checkAdvanvced);
        
        //Initializing Audio Video RadioButtons
        rbAudio = (RadioButton) findViewById(R.id.audio);
        rbVideo = (RadioButton) findViewById(R.id.video);
        rbAudio.setEnabled(false);
        rbVideo.setEnabled(false);
        
        //Initializing ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Converting");
        
        //Initializing RangeSeekBar
        rsb = (RangeSeekBar) findViewById(R.id.rsbTime);
        rsb.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>(){
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                onTimeSelected(minValue, maxValue);
            }
            
        });
        rsb.setEnabled(false);
        
        //Initializing RangeSekBar Labels(TextView)
        tvStart = (TextView) findViewById(R.id.startTime);
        tvEnd = (TextView) findViewById(R.id.endTime);
        
        //Initializing Container Spinner
        containers = (Spinner)findViewById(R.id.format);
        containers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                String container = parent.getItemAtPosition(pos).toString();
                onContainerSelected(container);
            }

            @Override
            public void onNothingSelected(AdapterView<?> av) {
                
            }
        });
        containers.setEnabled(false);
        
        //Initializing Bitrate SeekBar
        sb = (SeekBar) findViewById(R.id.seekBitrate);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar sb, int i, boolean bln) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                Log.d(TAG, "SeekBar Touched");
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                bitrate = progress;
                options = options+"-ab,"+bitrate+",";
            }
            
        });
        sb.setEnabled(false);
        
        //Initializing Notification Components
        notif = new NotificationCompat.Builder(this);
        notif.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Conversion Finished");
        notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        notifyIntent = new Intent(this,NotifActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        //Initializing FFmpeg Object 
        ffmpeg = FFmpeg.getInstance(getApplicationContext());
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Extra Menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Load FFmpeg Binary">
    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Execute FFmpeg Binary">
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                    notifyIntent.putExtra("PREVIEW_FILE", "");
                    if(STATE == 1){
                        notif.setContentText("Your video could not be converted. Tap to know more.");
                        notifyPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notif.setContentIntent(notifyPendingIntent);
                        notifManager.notify(FINISH_NOTIFICATION, notif.build());
                    }
                    else{
                        Snackbar.make(findViewById(R.id.btnExecute), "Your video could not be converted. Tap to know more.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(notifyIntent);
                            }
                        })
                                .show();
                    }
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
                    notifyIntent.putExtra("PREVIEW_FILE", preview);
                    if(STATE == 1) {
                        notif.setContentText("Your video was converted succeffully. Tap to preview.");                        
                        notifyPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notif.setContentIntent(notifyPendingIntent);
                        notifManager.notify(FINISH_NOTIFICATION, notif.build());
                    }
                    else {
                        Snackbar.make(findViewById(R.id.btnExecute), "Your video was converted succeffully. Tap to preview.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(notifyIntent);
                            }
                        })
                                .show();
                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    Log.d(TAG, "progress : " + s);
                    progressDialog.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Select Video">
    public void selectVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Pick a Video"), PICK_VIDEO_REQUEST);
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="On Radio Button Choose">
    public void onChoose(View view){
        if(view.getId() == R.id.video){
            adapter = ArrayAdapter.createFromResource(this, R.array.video_containers, android.R.layout.simple_spinner_item);
            containers.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            containers.setEnabled(true);
        }
        else if(view.getId() == R.id.audio){
            adapter = ArrayAdapter.createFromResource(this, R.array.audio_containers, android.R.layout.simple_spinner_item);
            containers.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            containers.setEnabled(true);
        }
        rsb.setEnabled(true);
        sb.setEnabled(true);
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="On Container Selected Spinner">
    public void onContainerSelected(String container){
        if(container.equals("AVI")){
            options = "-vcodec,mpeg4,-acodec,libmp3lame,";
            ext = ".avi";
        } else if(container.equals("MP4")){
            options = "-vcodec,mpeg4,-acodec,aac,";
            ext = ".mp4";
        } else if(container.equals("MKV")){
            options = "-vcodec,libx264,-acodec,aac,";
            ext = ".mkv";
        } else if(container.equals("MOV")){
            options = "-vcodec,libx264,-acodec,aac,";
            ext = ".mov";
        } else if(container.equals("3GP")){
            options = "-vcodec,mpeg4,-acodec,aac,";
            ext = ".3gp";
        } else if(container.equals("FLV")){
            options = "-vcodec,flv,-acodec,aac,";
            ext = ".flv";
        } else if(container.equals("MPG")){
            options = "-vcodec,mpeg2video,-acodec,libmp3lame,";
            ext = ".mpg";
        } else if(container.equals("WMV")){
            options = "-vcodec,wmv2,-acodec,wmav2,";
            ext = ".wmv";
        } else if(container.equals("OGG")){
            options = "-vcodec,libtheora,-acodec,libvorbis,";
            ext = ".ogg";
        } else if(container.equals("MP3")){
            options = "-vn,-acodec,libmp3lame,";
            ext = ".mp3";
        } else if(container.equals("M4A")){
            options = "-vn,-acodec,aac,";
            ext = ".m4a";
        } else if(container.equals("FLAC")){
            options = "-vn,-acodec,flac,";
            ext = ".flac";
        } else if(container.equals("AAC")){
            options = "-vn,-acodec,aac,";
            ext = ".aac";
        } else if(container.equals("AMR")){
            options = "-vn,-acodec,amr,";
            ext = ".amr";
        } else if(container.equals("WAV")){
            options = "-vn,-acodec,pcm_s16le,";
            ext = ".wav";
        } else if(container.equals("AIFF")){
            options = "-vn,-acodec,pcm_s16be,";
            ext = ".aiff";
        }
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="On Duration Selected">
    public void onTimeSelected(long min, long max) {
        clip = "-ss,"+min+",-to,"+max+",";
        String time = convertToTime(min);
        tvStart.setText(time);
        time = convertToTime(max);
        tvEnd.setText(time);
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Execute Function">
    public void execute(View view){
        if(selectedImagePath != null && selectedImagePath.length() > 0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String postName = sdf.format(new Date());
            String cmd;
            
            if(filename != null && filename.length() >0){
                cmd = "-i,"+selectedImagePath+","+options+","+outPath+"/"+filename;
                preview = outPath+"/"+filename;
            }
            else{
                cmd = "-i,"+selectedImagePath+","+clip+options+outPath+"/vid-"+postName+ext;
                preview = outPath+"/vid-"+postName+ext;
            }
            
            String[] command = cmd.split(",");
            
            if (command.length != 0) {
                execFFmpegBinary(command);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
            }
        }
        else{
            Snackbar.make(view, "Select a video first", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="On Advanced Option Selected">
    public void onAdvancedSelected(View view) {
        if(cb.isChecked()){
            Intent intent = new Intent(MainActivity.this, AdvancedOptions.class);
            if(selectedImagePath != null && selectedImagePath.length() > 0)
                intent.putExtra("VIDEO_PATH", selectedImagePath);
            startActivityForResult(intent, ADVANCED_OPTIONS_REQUEST);
        }
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Override On Activity Result">
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            String[] projection = { MediaStore.Video.Media.DATA };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            selectedImagePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            
            rbAudio.setEnabled(true);
            rbVideo.setEnabled(true);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(selectedImagePath);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInSecs = Long.parseLong(time);
            timeInSecs /= 1000;
            String bit = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            maxBitrate = Integer.parseInt(bit);
            
            rsb.setRangeValues(0l, timeInSecs);
            tvStart.setText(convertToTime(0));
            tvEnd.setText(convertToTime(timeInSecs));
            
            sb.setMax(maxBitrate);
            sb.setProgress(sb.getMax());
        }
        else if(requestCode == ADVANCED_OPTIONS_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null){
            options = data.getExtras().getString("OPTIONS");
            filename = data.getExtras().getString("FILENAME");
            selectedImagePath = data.getExtras().getString("VIDEO_PATH");
            if(options == null || filename == null || options.length() == 0 || filename.length() == 0){
                cb.setChecked(false);
            }
        }
        else if(requestCode == ADVANCED_OPTIONS_REQUEST && resultCode == Activity.RESULT_CANCELED){
            cb.setChecked(false);
        }
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Convert to hh:mm:ss">
    public String convertToTime(long sec) {
        long hh, mm, ss;
        hh = sec/(60*60);
        mm = sec/60 - hh*60;
        ss = sec - (mm*60) - (hh*60*60);
        return hh+":"+mm+":"+ss;
    }//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Show FFmpeg Not Supported">
    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create()
                .show();

    }//</editor-fold>
}
