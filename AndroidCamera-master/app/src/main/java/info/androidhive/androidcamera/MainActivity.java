package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.wang.avi.AVLoadingIndicatorView;

//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import info.androidhive.androidcamera.enums.ConnectionEnums;
import info.androidhive.androidcamera.face_tracking.FaceTrackerActivity;
import info.androidhive.androidcamera.utility.*;
import info.androidhive.androidcamera.video_recording.Camera2VideoFragment;

public class MainActivity extends AppCompatActivity {
//    public static final int REQUEST_CODE_CAPTURE_PERM = 1234;
//    private MediaProjectionManager mProjectionManager;
//    private int mScreenDensity;
    private long signatureStartTime;
    private long signatureEndTime;
    SignaturePad mSignaturePad;
    PDFView pdfView;
    RelativeLayout inner_layout;
    // Activity request codes

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    //ViewPager viewPager;
    //Camera2VideoFragment camera2VideoFragment;

//    private static final String VIDEO_MIME_TYPE = "video/avc";
//    private static final int VIDEO_WIDTH = 720;
//    private static final int VIDEO_HEIGHT = 1280;
    private Button saveButton;
    private boolean isPadSigned = false;
    //
    private boolean mMuxerStarted = false;
    //private MediaProjection mMediaProjection;
    //private Surface mInputSurface;
    //private MediaMuxer mMuxer;
    //private MediaCodec mVideoEncoder;
    //private MediaCodec.BufferInfo mVideoBufferInfo;
    private int mTrackIndex = -1;
    private ProgressDialog progressDialog;
    private Button reset_button;
    private AVLoadingIndicatorView avindicatorview;
    //private final Handler mDrainHandler = new Handler(Looper.getMainLooper());
//    private Runnable mDrainEncoderRunnable = new Runnable() {
//        @Override
//        public void run() {
//            drainEncoder();
//        }
//    };
    //private String filePathForScreenRecordingVideo="";

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        //mProjectionManager = (MediaProjectionManager)getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);
        saveButton = findViewById(R.id.save_button);
        reset_button = findViewById(R.id.reset_button);
        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        inner_layout = findViewById(R.id.inner_layout);
        avindicatorview = findViewById(R.id.avindicatorview);
        avindicatorview.show();
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ScreenRecordingInitiationActivity.mMediaRecorder.resume();
                } else {
                    signatureStartTime = System.currentTimeMillis()-padding;
                }
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                isPadSigned = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ScreenRecordingInitiationActivity.mMediaRecorder.pause();
                } else {
                    signatureEndTime = System.currentTimeMillis()+padding;
                }

            }

            @Override
            public void onClear() {
                isPadSigned = false;
                //Event triggered when the pad is cleared
            }
        });

        //downloadSampleVideoToDisplayForScreenRecording();
        downloadFileForTheGivenMobileNumber();
        //viewPager = (ViewPager) findViewById(R.id.viewpagercamera);
        //camera2VideoFragment = Camera2VideoFragment.newInstance();

        //setupViewPager(viewPager);

    }

    long padding = 500; // 1/2 seconds

//    private void downloadSampleVideoToDisplayForScreenRecording(){
//        progressDialog = ProgressDialog.show(this, "", "Downloading File...");
//        String url = ApplicationConstants.BASE_URL+"/getDocument/samplevideo";
//        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
//                new Response.Listener<byte[]>() {
//                    @Override
//                    public void onResponse(byte[] response) {
//                        // TODO handle the response
//                        progressDialog.dismiss();
//                        try {
//                            if (response!=null) {
//                                FileOutputStream outputStream;
//                                String fileName = Utils.getRootPathOfApp(getApplicationContext());
//                                String timeStamp = String.valueOf(System.currentTimeMillis());
//                                filePathForScreenRecordingVideo = fileName+"/"+"Screen_recording_"+timeStamp+".mp4";
//                                File file = new File(filePathForScreenRecordingVideo);
//                                GlobalVariables.screenRecordingVideoFilePath = filePathForScreenRecordingVideo;
//                                file.createNewFile();
//                                outputStream = new FileOutputStream(file);
//                                outputStream.write(response);
//                                outputStream.close();
//                                downloadFileForTheGivenMobileNumber();
//                            }
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
//                            Toast.makeText(MainActivity.this, "Some problem in file downloading.", Toast.LENGTH_SHORT).show();
//                            MainActivity.this.finish();
//                            progressDialog.dismiss();
//                            e.printStackTrace();
//                        }
//                    }
//                } ,new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // TODO handle the error
//                progressDialog.dismiss();
//                GlobalVariables.connectionEnums = ConnectionEnums.SERVER_DOWN;
//                if (error instanceof NoConnectionError) {
//                    GlobalVariables.connectionEnums = ConnectionEnums.NO_INTERNET_CONNECTION;
//                    Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
//                } else if (error instanceof TimeoutError){
//                    Toast.makeText(MainActivity.this, "Unable to connect to server.", Toast.LENGTH_LONG).show();
//                }
//                startActivity(new Intent(MainActivity.this, ConnectionFailureActivity.class));
//               // Toast.makeText(MainActivity.this, "Unable to download file", Toast.LENGTH_SHORT).show();
//                MainActivity.this.finish();
//                error.printStackTrace();
//            }
//        }, null);
//        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
//        mRequestQueue.add(request);
//    }

    private void downloadFileForTheGivenMobileNumber(){
        progressDialog = ProgressDialog.show(this, "", "Downloading File...");
        //MyCountDownTimer myCountDownTimer = new MyCountDownTimer(1000, 500);
        //myCountDownTimer.start();
        String url = ApplicationConstants.BASE_URL+"/getDocument/"+getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE)+"/"+
            getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER);
//        HashMap<String, String> params = new HashMap<>();
//        params.put("mobileNumber", getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
//        params.put("countryCode", getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        progressDialog.dismiss();
                        try {
                            if (response!=null) {
                                FileOutputStream outputStream;
                                String dirPath = Utils.getRootPathOfApp(getApplicationContext());
                                String timeStamp = String.valueOf(System.currentTimeMillis());
                                String filePath = dirPath+"/"+timeStamp+".pdf";
                                File file = new File(filePath);
                                file.createNewFile();
                                outputStream = new FileOutputStream(file);
                                outputStream.write(response);
                                outputStream.close();
                                Toast.makeText(MainActivity.this, "Download complete.", Toast.LENGTH_SHORT).show();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    ScreenRecordingInitiationActivity.mMediaRecorder.pause();
                                } else {
                                    GlobalVariables.screenRecordingTimesInMillisisecondForCropping.add(System.currentTimeMillis());
                                }

                                loadFile(file);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            Toast.makeText(MainActivity.this, "Some problem in file downloading.", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                progressDialog.dismiss();
                GlobalVariables.connectionEnums = ConnectionEnums.SERVER_DOWN;
                if (error instanceof NoConnectionError) {
                    Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
                    GlobalVariables.connectionEnums = ConnectionEnums.NO_INTERNET_CONNECTION;
                } else if (error instanceof TimeoutError){
                    Toast.makeText(MainActivity.this, "Unable to connect to server.", Toast.LENGTH_LONG).show();
                }
                startActivity(new Intent(MainActivity.this, ConnectionFailureActivity.class));
               // Toast.makeText(MainActivity.this, "Unable to download file", Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
                error.printStackTrace();
            }
        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);

    }


//    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public void onStartScreenRecording(View view) {
//        Intent permissionIntent = mProjectionManager.createScreenCaptureIntent();
//        startActivityForResult(permissionIntent, REQUEST_CODE_CAPTURE_PERM);
//    }


//    public void onStopScreenRecording(View v) {
//        releaseEncoders();
//    }

//    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void startScreenRecording() {
//
//
//        DisplayManager dm = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
//        Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
//        if (defaultDisplay == null) {
//            throw new RuntimeException("No display found.");
//        }
//        prepareVideoEncoder();
//
//        try {
//
//            //mMuxer = new MediaMuxer("/storage/emulated/0/DCIM/Camera/video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            mMuxer = new MediaMuxer(filePathForScreenRecordingVideo, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        } catch (IOException ioe) {
//            throw new RuntimeException("MediaMuxer creation failed", ioe);
//        }
//
//        // Get the display size and density.
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int cameraSourcePreviewWidth = metrics.widthPixels;
//        int cameraSourcePreviewHeight = metrics.heightPixels;
//        int screenDensity = metrics.densityDpi;
//
//        // Start the video input.
//        mMediaProjection.createVirtualDisplay("Recording Display", cameraSourcePreviewWidth,
//                cameraSourcePreviewHeight, screenDensity, 0 /* flags */, mInputSurface,
//                null /* callback */, null /* handler */);
//
//        // Start the encoders
//        drainEncoder();
//    }
//
//     @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private void prepareVideoEncoder() {
//        mVideoBufferInfo = new MediaCodec.BufferInfo();
//        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
//        int frameRate = 30; // 30 fps
//
//        // Set some required properties. The media codec may fail if these aren't defined.
//        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000); // 6Mbps
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
//        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate);
//        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate);
//        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
//        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds between I-frames
//
//        // Create a MediaCodec encoder and configure it. Get a Surface we can use for recording into.
//        try {
//            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
//            mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            mInputSurface = mVideoEncoder.createInputSurface();
//            mVideoEncoder.start();
//        } catch (IOException e) {
//            releaseEncoders();
//        }
//    }
//
////    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void releaseEncoders() {
//        mDrainHandler.removeCallbacks(mDrainEncoderRunnable);
//        if (mMuxer != null) {
//            if (mMuxerStarted) {
//                mMuxer.stop();
//            }
//            mMuxer.release();
//            mMuxer = null;
//            mMuxerStarted = false;
//        }
//        if (mVideoEncoder != null) {
//            mVideoEncoder.stop();
//            mVideoEncoder.release();
//            mVideoEncoder = null;
//        }
//        if (mInputSurface != null) {
//            mInputSurface.release();
//            mInputSurface = null;
//        }
//        if (mMediaProjection != null) {
//            mMediaProjection.stop();
//            mMediaProjection = null;
//        }
//        mVideoBufferInfo = null;
//        mDrainEncoderRunnable = null;
//        mTrackIndex = -1;
//    }
//
//    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private boolean drainEncoder() {
//        mDrainHandler.removeCallbacks(mDrainEncoderRunnable);
//        while (true) {
//            int bufferIndex = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, 0);
//
//            if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                // nothing available yet
//                break;
//            } else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                // should happen before receiving buffers, and should only happen once
//                if (mTrackIndex >= 0) {
//                    throw new RuntimeException("format changed twice");
//                }
//                mTrackIndex = mMuxer.addTrack(mVideoEncoder.getOutputFormat());
//                if (!mMuxerStarted && mTrackIndex >= 0) {
//                    mMuxer.start();
//                    mMuxerStarted = true;
//                }
//            } else if (bufferIndex < 0) {
//                // not sure what's going on, ignore it
//            } else {
//                ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(bufferIndex);
//                if (encodedData == null) {
//                    throw new RuntimeException("couldn't fetch buffer at index " + bufferIndex);
//                }
//
//                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    mVideoBufferInfo.size = 0;
//                }
//
//                if (mVideoBufferInfo.size != 0) {
//                    if (mMuxerStarted) {
//                        encodedData.position(mVideoBufferInfo.offset);
//                        encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
//                        mMuxer.writeSampleData(mTrackIndex, encodedData, mVideoBufferInfo);
//                    } else {
//                        // muxer not started
//                    }
//                }
//
//                mVideoEncoder.releaseOutputBuffer(bufferIndex, false);
//
//                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    break;
//                }
//            }
//        }
//
//        mDrainHandler.postDelayed(mDrainEncoderRunnable, 10);
//        return false;
//    }


    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        //outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        //imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//         if (REQUEST_CODE_CAPTURE_PERM == requestCode) {
//            if (resultCode == RESULT_OK) {
//                mMediaProjection = (MediaProjection) mProjectionManager.getMediaProjection(resultCode, data);
//                //camera2VideoFragment.startRecordingVideo();
//
//                startScreenRecording(); // defined below
//            } else {
////                Toast.makeText(this, "Screen recording is mandatory", Toast.LENGTH_SHORT).show();
////                finish();
//                // user did not grant permissions
//            }
//        }
    }


    public void onResetSignaturePad(View view) {
        mSignaturePad.clear();
    }

    public void onSaveSignatureAndCloseApplication(View view) {
        if (isPadSigned){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ScreenRecordingInitiationActivity.mMediaRecorder.resume();
            } else {
                GlobalVariables.screenRecordingTimesInMillisisecondForCropping.add(signatureStartTime);
                GlobalVariables.screenRecordingTimesInMillisisecondForCropping.add(signatureEndTime);
                GlobalVariables.screenRecordingTimesInMillisisecondForCropping.add(System.currentTimeMillis());
            }
            //camera2VideoFragment.stopRecordingVideo();
//            onStopScreenRecording(view);
            inner_layout.setVisibility(View.GONE);

            reset_button.setEnabled(false);
            saveButton.setEnabled(false);
            mSignaturePad.setEnabled(false);
            Bitmap signBitmap = Utils.rescaleBitmapWidthHeight(mSignaturePad.getSignatureBitmap(), ApplicationConstants.IMAGE_SIZE);
            String prefixName = getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER) + "_signature";
            GlobalVariables.signatureImagePath = Utils.storeImage(signBitmap, this, 100, false, prefixName);

            startActivity();
            finish();

        } else {
            Toast.makeText(this, "Please sign the pad.", Toast.LENGTH_SHORT).show();
        }

    }

    private void startActivity(){
        Intent intent = new Intent(this, FaceTrackerActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
        intent.putExtra(ApplicationConstants.POSITION, ApplicationConstants.END);
        startActivity(intent);
    }

    public void onFullScreenMode(View view) {

    }

    public void loadFile(File file){

        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setVisibility(View.VISIBLE);
        //findViewById(R.id.download_file_button).setVisibility(View.GONE);
        pdfView.fromFile(file).load();

//        onStartScreenRecording(null);
    }

    public void loadFile() {
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setVisibility(View.VISIBLE);
        //findViewById(R.id.download_file_button).setVisibility(View.GONE);

        pdfView.fromFile(new File("/storage/emulated/0/DCIM/temp.pdf"))
                .load();
//        onStartScreenRecording(null);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        //adapter.addFragment(new CameraImage(), "Camera");
        adapter.addFragment(Camera2VideoFragment.newInstance(), "Camera");
        //adapter.addFragment(new FaceTrackerFragment(), "Face Tracker");
//
//       /* adapter.addFragment(new FourFragment(), "FOUR");
//        adapter.addFragment(new FiveFragment(), "FIVE");
//        adapter.addFragment(new SixFragment(), "SIX");*/
        viewPager.setAdapter(adapter);
    }

    private class MyCountDownTimer extends CountDownTimer {


        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            progressDialog.dismiss();
            loadFile();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
