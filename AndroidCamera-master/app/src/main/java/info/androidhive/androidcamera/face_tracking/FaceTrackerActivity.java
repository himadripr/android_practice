package info.androidhive.androidcamera.face_tracking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.JsonObject;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.NameValuePair;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//
//
//import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import info.androidhive.androidcamera.ApplicationConstants;
import info.androidhive.androidcamera.GlobalVariables;
import info.androidhive.androidcamera.MainActivity;
import info.androidhive.androidcamera.R;
import info.androidhive.androidcamera.utility.GPSTracker;
import info.androidhive.androidcamera.utility.Utils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";
    private boolean imageCaptureFlag = false;
    private CameraSource mCameraSource = null;
    //private TextView text;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private TextView mUpdates;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_face_tracker);

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        mUpdates = (TextView) findViewById(R.id.faceUpdates);


        if (getIntent().getStringExtra(ApplicationConstants.POSITION).equals(ApplicationConstants.START)){

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Scan your face again to complete the process");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face detector dependencies are not yet available.")
                    .show();

            Log.w(TAG, "Face detector dependencies are not yet available.");
            return;
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1024, 720)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void startActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
        startActivityForResult(intent,
                MainActivity.REQUEST_CODE_CAPTURE_PERM);
    }

    private boolean getGpsLocationANDTimeStamp(){
        GPSTracker gpsTracker = new GPSTracker(FaceTrackerActivity.this);
        if (gpsTracker.canGetLocation()){
            DecimalFormat precision = new DecimalFormat("0.00");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss");
            if (getIntent().getStringExtra(ApplicationConstants.POSITION).equals(ApplicationConstants.START)){
                GlobalVariables.startingLatitudes = precision.format(gpsTracker.getLatitude());
                GlobalVariables.startingLongitudes = precision.format(gpsTracker.getLongitude());
                GlobalVariables.startingTime = simpleDateFormat.format(new Date());
            } else if (getIntent().getStringExtra(ApplicationConstants.POSITION).equals(ApplicationConstants.END)){
                GlobalVariables.endingLatitudes = precision.format(gpsTracker.getLatitude());
                GlobalVariables.endingLongitudes = precision.format(gpsTracker.getLongitude());
                GlobalVariables.endingTime = simpleDateFormat.format(new Date());
            }
            gpsTracker.stopUsingGPS();
            return true;
        } else {
            gpsTracker.showSettingsAlert();
            return false;
        }
    }

//    private static Bitmap rescaleBitmapWidthHeight(Bitmap bitmap){
//        int width, height;
//        if (bitmap.getWidth()>bitmap.getHeight()){
//            width=100;
//            height = (int)(width*((float)bitmap.getHeight()/(float)bitmap.getWidth()));
//        } else{
//            height = 100;//1040
//            width = (int)(height*((float)bitmap.getWidth()/(float)bitmap.getHeight()));
//        }
//        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
//        return bitmap;
//    }

    private void captureImage(){
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bitmapCaptured = bmp.copy(Bitmap.Config.ARGB_8888, true);

                bitmapCaptured = Utils.rescaleBitmapWidthHeight(bitmapCaptured, 100);
                imageCaptureFlag = false;
                String filePath = Utils.storeImage(bitmapCaptured, getApplicationContext(), 100);

                if (filePath!=null){
                    // Toast.makeText(FaceTrackerActivity.this, "Image is saved at location : "+filePath, Toast.LENGTH_LONG).show();

                    if (FaceTrackerActivity.this.getIntent().getStringExtra(ApplicationConstants.POSITION).equals(ApplicationConstants.START)){
                        GlobalVariables.startingImageFilePath = filePath;
                        startActivity();
                    } else if (FaceTrackerActivity.this.getIntent().getStringExtra(ApplicationConstants.POSITION).equals(ApplicationConstants.END)){
                        GlobalVariables.endingImageFilePath = filePath;
                        try {
                            uploadFilesToServer();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            FaceTrackerActivity.this.finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            FaceTrackerActivity.this.finish();
                        }
                    }
                } else {
                    Toast.makeText(FaceTrackerActivity.this, "Some problem has occurred", Toast.LENGTH_SHORT).show();
                    FaceTrackerActivity.this.finish();
                }

            }
        });
    }

    ProgressDialog progressDialog;


    private void uploadFilesToServer() throws ExecutionException, InterruptedException {
        progressDialog = ProgressDialog.show(this, "", "Processing...");
        new AsyncPostCall(this).execute();
    }

    public void finishFaceTrackerActivity(){
        progressDialog.dismiss();
        this.finish();
    }
    private class AsyncPostCall extends AsyncTask<Void, Void, Boolean> {
        WeakReference<Activity> activityWeakReference;
        ArrayList<Exception> exceptions;

        AsyncPostCall(Activity activity){
            activityWeakReference = new WeakReference<Activity>(activity);
            exceptions = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
//            List<Part> files = new ArrayList<>();
//            files.add(new FilePart("screenRecording", new File(GlobalVariables.screenRecordingVideoFilePath)));
//            files.add(new FilePart("signature", new File(GlobalVariables.signatureImagePath)));
//            files.add(new FilePart("startingImage", new File(GlobalVariables.startingImageFilePath)));
//            files.add(new FilePart("endingImage", new File(GlobalVariables.endingImageFilePath)));
//            try {
//                String string = Ion
//                        .with(activityWeakReference.get())
//                        .load(ApplicationConstants.BASE_URL+"/document/upload")
//                        .setTimeout(60*60*1000)
//                        .setMultipartParameter("mobileNumber", GlobalVariables.mobileNumber)
//                        .setMultipartParameter("startingLatitude", GlobalVariables.startingLatitudes)
//                        .setMultipartParameter("startingLongitude", GlobalVariables.startingLongitudes)
//                        .setMultipartParameter("startingDateTime", GlobalVariables.startingTime)
//                        .setMultipartParameter("endingLatitude", GlobalVariables.endingLatitudes)
//                        .setMultipartParameter("endingLongitude", GlobalVariables.endingLongitudes)
//                        .setMultipartParameter("endingDateTime", GlobalVariables.endingTime)
//                        .addMultipartParts(files)
//
//                        .asString()
//                        .get();
//                System.out.println();
//
//                if (string.equals("success")){
//                    return true;
//                }
//            } catch (InterruptedException e) {
//                exceptions.add(e);
//                e.printStackTrace();
//            } catch (Exception e) {
//                exceptions.add(e);
//                e.printStackTrace();
//
//            }
            uploadData();
            return true;
        }

//        public String uploadFiles() throws IOException {
//            HttpClient httpclient = HttpClients.createDefault();
//            HttpPost httppost = new HttpPost("http://www.a-domain.com/foo/");
//
//// Request parameters and other properties.
//            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//            params.add(new BasicNameValuePair("param-1", "12345"));
//            params.add(new BasicNameValuePair("param-2", "Hello!"));
//
//
//
//            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//
////Execute and get the response.
//            HttpResponse response = httpclient.execute(httppost);
//            HttpEntity entity = response.getEntity();
//
//
//
//
//            File file = new File("data.zip");
//            FileBody fileBody = new FileBody(file, "image/jpg");
//            MultipartEntity multipartEntity = new MultipartEntity();
//
//
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            builder.addPart("file", fileBody);
//            HttpEntity entity = builder.build();
//
//            HttpPost request = new HttpPost("http://localhost:8080/upload");
//            request.setEntity(entity);
//
//            HttpClient client = HttpClientBuilder.create().build();
//
//
//
//            if (entity != null) {
//                try (InputStream instream = entity.getContent()) {
//                    // do something useful
//                    return "success";
//                }
//
//            }
//            return null;
//        }

        public String uploadData() {

            try {

                final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
                final MediaType MEDIA_TYPE_MP4 = MediaType.parse("video/mp4");


                RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                        .addFormDataPart("userid", "8457851245")
//                        .addFormDataPart("userfile","profile.png", RequestBody.create(MEDIA_TYPE_JPG, file))

                        .addFormDataPart("screenRecording","screenRecording.mp4", RequestBody.create(MEDIA_TYPE_MP4, new File(GlobalVariables.screenRecordingVideoFilePath)))
                        .addFormDataPart("signature","signature.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.signatureImagePath)))
                        .addFormDataPart("startingImage","startingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.startingImageFilePath)))
                        .addFormDataPart("endingImage","endingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.endingImageFilePath)))

                        .addFormDataPart("mobileNumber", GlobalVariables.mobileNumber)
                        .addFormDataPart("startingLatitude", GlobalVariables.startingLatitudes)
                        .addFormDataPart("startingLongitude", GlobalVariables.startingLongitudes)
                        .addFormDataPart("startingDateTime", GlobalVariables.startingTime)
                        .addFormDataPart("endingLatitude", GlobalVariables.endingLatitudes)
                        .addFormDataPart("endingLongitude", GlobalVariables.endingLongitudes)
                        .addFormDataPart("endingDateTime", GlobalVariables.endingTime)

                        .build();

                Request request = new Request.Builder()

                        .url(ApplicationConstants.BASE_URL+"/document/upload")

                        .post(req)

                        .build();

                OkHttpClient client;
                client = new OkHttpClient.Builder().build();
                Response response = client.newCall(request).execute();

                Log.d("response", "uploadImage:"+response.body().string());
                String result = response.body().string();
//                return new JSONObject(response.body().string());
                return response.body().string();

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                exceptions.add(e);
                Log.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                exceptions.add(e);
                Log.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);

            if (flag){
                Toast.makeText(activityWeakReference.get(), "Process completed...", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(activityWeakReference.get(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                for (Exception e: exceptions){
                    Toast.makeText(FaceTrackerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            finishFaceTrackerActivity();

        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_CODE_CAPTURE_PERM){
            this.setResult(resultCode);

        }
        finish();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void onScanButtonClick(View view) {
        if (getGpsLocationANDTimeStamp()){
            imageCaptureFlag = true;
            Button scanButton = findViewById(R.id.button_scan);
            scanButton.setEnabled(false);
            scanButton.setVisibility(View.INVISIBLE);
        }


    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay,FaceTrackerActivity.this);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay,Context context) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay,context);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            if (imageCaptureFlag){
                mFaceGraphic.setCircleNeedsToBeShown(true);
                if (mFaceGraphic.isFaceUpfrontAndUpright()) {
                    captureImage();
                }
            }

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }


}
