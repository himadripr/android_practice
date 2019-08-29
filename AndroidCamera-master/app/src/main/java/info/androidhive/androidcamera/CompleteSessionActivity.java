package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.vision.text.Line;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutionException;

import info.androidhive.androidcamera.enums.ConnectionEnums;
import info.androidhive.androidcamera.interfaces.PostCallResponseHandler;
import info.androidhive.androidcamera.interfaces.ProcessAfterCheckingInternetConnection;
import info.androidhive.androidcamera.utility.AsyncPostCall;
import info.androidhive.androidcamera.utility.Utils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompleteSessionActivity extends AppCompatActivity {
    LinearLayout root_layout;
    RelativeLayout temp_layout;
    //AVLoadingIndicatorView avindicatorview;
    Button button_complete_session, button_submit;
    DonutProgress donut_progress;
    TextView information_text_view, size_textview;
    //boolean isDataSubmittedToServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_session);
        root_layout = findViewById(R.id.root_layout);
        //avindicatorview = findViewById(R.id.avindicatorview);
        temp_layout = findViewById(R.id.temp_layout);
        button_complete_session = findViewById(R.id.button_complete_session);
        button_submit = findViewById(R.id.button_submit);
        button_submit.setVisibility(View.GONE);
        temp_layout.setVisibility(View.VISIBLE);
        donut_progress = findViewById(R.id.donut_progress);
        information_text_view = findViewById(R.id.information_text_view);
        size_textview = findViewById(R.id.size_textview);
        GlobalVariables.signedDocumentFilePath = null;
//        MyCountDownTimer myCountDownTimer = new MyCountDownTimer(4000, 1000);
//        myCountDownTimer.start();
        checkInternetConnection();

    }

    private void setScreenForDownloadingOrUploading(){
        size_textview.setText("");
        button_submit.setVisibility(View.GONE);
        root_layout.setVisibility(View.INVISIBLE);
        donut_progress.setVisibility(View.VISIBLE);
        donut_progress.setDonut_progress("0");
        temp_layout.setVisibility(View.VISIBLE);
    }

    private void checkInternetConnection(){
        setScreenForDownloadingOrUploading();
        Utils.isInternetConnectionAvailable(new ProcessAfterCheckingInternetConnection() {
            @Override
            public void processRequest(boolean connectionStatus, ConnectionEnums connectionEnums) {
                if (connectionStatus){
                    try {
                        uploadFilesToServer();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    switch (connectionEnums){
                        case NO_INTERNET_CONNECTION:
                            Toast.makeText(CompleteSessionActivity.this, "no internet connection", Toast.LENGTH_LONG).show();
                            break;
                        case SERVER_DOWN:
                            Toast.makeText(CompleteSessionActivity.this, "unable to connect to server", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(CompleteSessionActivity.this, "some unknown problem while connection", Toast.LENGTH_LONG).show();

                    }
                    resetScreen();
                }
            }
        }, getApplicationContext());
    }

    public void resetScreen(){
        root_layout.setVisibility(View.INVISIBLE);
        //avindicatorview.hide();
        size_textview.setText("");
        donut_progress.setVisibility(View.GONE);
        temp_layout.setVisibility(View.INVISIBLE);
        button_submit.setVisibility(View.VISIBLE);
    }

    public void onViewDocumentClick(View view) {
        //Toast.makeText(this, "Document", Toast.LENGTH_SHORT).show();
        String newFilePath = GlobalVariables.signedDocumentFilePath;
        if (newFilePath!=null){
            showSignedDocument(newFilePath);
        } else {
            downloadSignedDocument();
        }
    }

    private void showSignedDocument(String newFilePath){
        File file = new File(newFilePath);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    public void onWatchRecordingClick(View view) {
        //Toast.makeText(this, "Screen Recording", Toast.LENGTH_SHORT).show();
//        String newVideoPath = Utils.getRootPathOfApp(this)+"/1.mp4";
        String newVideoPath = GlobalVariables.screenRecordingVideoFilePath;
        if (newVideoPath!=null){
            File file = new File(newVideoPath);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    //ProgressDialog progressDialog;



    private void uploadFilesToServer() throws ExecutionException, InterruptedException {
        //progressDialog = ProgressDialog.show(this, "", "Processing...");
        //avindicatorview.show();


//        new AsyncPostCall(this, new PostCallResponseHandler() {
//            @Override
//            public void processResponse(Response response) {
//                processAfterPostCall();
//            }
//        }).execute();
        uploadFilesToServerTemp();
    }

    private void uploadFilesToServerTemp(){
//        String dirPath = Utils.getRootPathOfApp(getApplicationContext());
//
//        String fileName = dirPath+"/"+"vid.mp4";
//        File file = new File(fileName);
//
//
//        RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
////                        .addFormDataPart("userid", "8457851245")
////                        .addFormDataPart("userfile","profile.png", RequestBody.create(MEDIA_TYPE_JPG, file))
//
//                .addFormDataPart("screenRecording","screenRecording.mp4", RequestBody.create(MEDIA_TYPE_MP4, new File(GlobalVariables.screenRecordingVideoFilePath)))
//                .addFormDataPart("signature","signature.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.signatureImagePath)))
//                .addFormDataPart("startingImage","startingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.startingImageFilePath)))
//                .addFormDataPart("endingImage","endingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.endingImageFilePath)))
//
//                .addFormDataPart("mobileNumber", GlobalVariables.mobileNumber)
//                .addFormDataPart("startingLatitude", GlobalVariables.startingLatitudes)
//                .addFormDataPart("startingLongitude", GlobalVariables.startingLongitudes)
//                .addFormDataPart("startingDateTime", GlobalVariables.startingTime)
//                .addFormDataPart("endingLatitude", GlobalVariables.endingLatitudes)
//                .addFormDataPart("endingLongitude", GlobalVariables.endingLongitudes)
//                .addFormDataPart("endingDateTime", GlobalVariables.endingTime)
//
//                .build();

//
        String url = ApplicationConstants.BASE_URL+"/document/upload";
        AndroidNetworking.upload(url)
                .addMultipartFile("screenRecording",new File(GlobalVariables.screenRecordingVideoFilePath))
                .addMultipartFile("signature", new File(GlobalVariables.signatureImagePath))
                .addMultipartFile("startingImage", new File(GlobalVariables.startingImageFilePath))
                .addMultipartFile("endingImage", new File(GlobalVariables.endingImageFilePath))

                .addMultipartParameter("mobileNumber", GlobalVariables.mobileNumber)
                .addMultipartParameter("startingLatitude", GlobalVariables.startingLatitudes)
                .addMultipartParameter("startingLongitude", GlobalVariables.startingLongitudes)
                .addMultipartParameter("startingDateTime", GlobalVariables.startingTime)
                .addMultipartParameter("endingLatitude", GlobalVariables.endingLatitudes)
                .addMultipartParameter("endingLongitude", GlobalVariables.endingLongitudes)
                .addMultipartParameter("endingDateTime", GlobalVariables.endingTime)

                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        double bytesUploadedInMB = (double)bytesUploaded/1000000.0;
                        String bytesUploadedInMBString = String.format("%.2f", bytesUploadedInMB);
                        double totalBytesInMB = (double)totalBytes/1000000.0;
                        String totalBytesInMBString = String.format("%.2f", totalBytesInMB);
                        size_textview.setText(bytesUploadedInMBString+"MB / "+totalBytesInMBString+"MB");
                        int per = (int)((double)bytesUploaded/(double)totalBytes*100.0);
                        donut_progress.setDonut_progress(String.valueOf(per));
                        //System.out.print(bytesUploaded);
                    }
                })
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        // do anything with response
                        System.out.println();
                        Toast.makeText(CompleteSessionActivity.this, response, Toast.LENGTH_SHORT).show();
                        processAfterPostCall();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        error.printStackTrace();
                        Toast.makeText(CompleteSessionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        processAfterPostCall();
                    }
                });
    }

    public void downloadSignedDocument(){
        String url = ApplicationConstants.BASE_URL+"/getSignedDocument/"+getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE)+"/"+
                getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER);
        information_text_view.setText("Downloading Signed document.");
        setScreenForDownloadingOrUploading();
        final String dirPath = Utils.getRootPathOfApp(getApplicationContext());
        String timeStamp = String.valueOf(System.currentTimeMillis());
        final String fileName = "Signed_"+timeStamp+".pdf";

        AndroidNetworking.download(url,dirPath,fileName)
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        // do anything with progress
                        double bytesDownloadedInMB = (double)bytesDownloaded/1000000.0;
                        String bytesDownloadedInMBString = String.format("%.2f", bytesDownloadedInMB);
                        double totalBytesInMB = (double)totalBytes/1000000.0;
                        String totalBytesInMBString = String.format("%.2f", totalBytesInMB);
                        size_textview.setText(bytesDownloadedInMBString+"MB / "+totalBytesInMBString+"MB");
                        int per = (int)((double)bytesDownloaded/(double)totalBytes*100.0);
                        donut_progress.setDonut_progress(String.valueOf(per));
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // do anything after completion
                        GlobalVariables.signedDocumentFilePath = dirPath+"/"+fileName;
                        processAfterPostCall();
                        showSignedDocument(GlobalVariables.signedDocumentFilePath);
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(CompleteSessionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        processAfterPostCall();
                    }
                });
    }



    public void processAfterPostCall(){
        //progressDialog.dismiss();
        //avindicatorview.hide();
        donut_progress.setVisibility(View.GONE);
        button_submit.setVisibility(View.GONE);
        root_layout.setVisibility(View.VISIBLE);
        temp_layout.setVisibility(View.INVISIBLE);
    }


    public void onCompleteSession(View view) {
        Toast.makeText(this, "Thank you.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LaunchingActivity.class));
        finish();

    }

    @Override
    public void onBackPressed() {

    }

    public void onSubmit(View view) {
        checkInternetConnection();
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
            checkInternetConnection();
        }
    }


}
