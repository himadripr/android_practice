package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.concurrent.ExecutionException;

import info.androidhive.androidcamera.enums.ConnectionEnums;
import info.androidhive.androidcamera.interfaces.PostCallResponseHandler;
import info.androidhive.androidcamera.interfaces.ProcessAfterCheckingInternetConnection;
import info.androidhive.androidcamera.utility.AsyncPostCall;
import info.androidhive.androidcamera.utility.Utils;
import okhttp3.Response;

public class CompleteSessionActivity extends AppCompatActivity {
    LinearLayout root_layout;
    RelativeLayout temp_layout;
    AVLoadingIndicatorView avindicatorview;
    Button button_complete_session, button_submit;
    //boolean isDataSubmittedToServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_session);
        root_layout = findViewById(R.id.root_layout);
        avindicatorview = findViewById(R.id.avindicatorview);
        temp_layout = findViewById(R.id.temp_layout);
        button_complete_session = findViewById(R.id.button_complete_session);
        button_submit = findViewById(R.id.button_submit);
        checkInternetConnection();

    }

    private void checkInternetConnection(){
        button_submit.setVisibility(View.GONE);
        temp_layout.setVisibility(View.VISIBLE);
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
        avindicatorview.hide();
        temp_layout.setVisibility(View.INVISIBLE);
        button_submit.setVisibility(View.VISIBLE);
    }

    public void onViewDocumentClick(View view) {
        //Toast.makeText(this, "Document", Toast.LENGTH_SHORT).show();
        String newFilePath = GlobalVariables.signedDocumentFilePath;
        if (newFilePath!=null){
            File file = new File(newFilePath);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
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
        avindicatorview.show();
        new AsyncPostCall(this, new PostCallResponseHandler() {
            @Override
            public void processResponse(Response response) {
                processAfterPostCall();
            }
        }).execute();
    }

    public void processAfterPostCall(){
        //progressDialog.dismiss();
        avindicatorview.hide();
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
}
