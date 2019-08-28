package info.androidhive.androidcamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import info.androidhive.androidcamera.utility.Utils;

public class ScreenRecordingInitiationActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
   // private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    Button btn_action;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720; //720
    private static final int DISPLAY_HEIGHT = 1280; //1280
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSION_KEY = 1;
    boolean isRecording = false;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recording_initiation);
        //screen recording
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        onToggleScreenShare();
    }

    public void onToggleScreenShare() {
        if (!isRecording) {
            initRecorder();
            shareScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            stopScreenSharing();
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            Intent intent = new Intent(mProjectionManager.createScreenCaptureIntent());
            startActivityForResult(intent, ApplicationConstants.REQUEST_CODE_FOR_SCREEN_RECORDING);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;

    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MobileNumberGetActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        try {
            ////mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            String fileName = Utils.getRootPathOfApp(getApplicationContext());
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String filePathForScreenRecordingVideo = fileName+"/"+"Screen_recording_"+timeStamp+".mp4";
            File file = new File(filePathForScreenRecordingVideo);
            GlobalVariables.screenRecordingVideoFilePath = filePathForScreenRecordingVideo;
            file.createNewFile();

            mMediaRecorder.setOutputFile(filePathForScreenRecordingVideo);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(24); // 30
            mMediaRecorder.setVideoEncodingBitRate(3000000);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        destroyMediaProjection();
        isRecording = false;


    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
        Toast.makeText(this, "Session recording saved.", Toast.LENGTH_SHORT).show();
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording = false;
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    private boolean isFirstTime = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApplicationConstants.REQUEST_CODE_FOR_SCREEN_RECORDING){
            this.setResult(resultCode);
            if (resultCode != RESULT_OK) {
                if (isFirstTime){

                    isFirstTime = false;
                }
                Toast.makeText(this, "Session Recording permission needed.", Toast.LENGTH_SHORT).show();

                isRecording = false;
                shareScreen();
                return;
            } else {
                mMediaProjectionCallback = new MediaProjectionCallback();
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                mVirtualDisplay = createVirtualDisplay();
                mMediaRecorder.start();
                isRecording = true;
                startOtpVerificationActivity();
            }


        } else if (requestCode == ApplicationConstants.REQUEST_CODE_FOR_MAIN_APPLICATION){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
            startCompleteSessionActivity();
            finish();

        } else {
            finish();
        }
    }

    private void startCompleteSessionActivity(){
        Intent intent = new Intent(this, CompleteSessionActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
        startActivity(intent);

    }

    private void startOtpVerificationActivity(){
        Intent intent = new Intent(this, OtpSendAndVerificationActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
        startActivityForResult(intent, ApplicationConstants.REQUEST_CODE_FOR_MAIN_APPLICATION);

    }

    @Override
    public void onBackPressed() {

    }
}
