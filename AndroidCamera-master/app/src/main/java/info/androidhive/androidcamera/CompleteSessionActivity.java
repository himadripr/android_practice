package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.concurrent.ExecutionException;

import info.androidhive.androidcamera.interfaces.PostCallResponseHandler;
import info.androidhive.androidcamera.utility.AsyncPostCall;
import info.androidhive.androidcamera.utility.Utils;
import okhttp3.Response;

public class CompleteSessionActivity extends AppCompatActivity {
    LinearLayout root_layout;
    RelativeLayout temp_layout;
    AVLoadingIndicatorView avindicatorview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_session);
        root_layout = findViewById(R.id.root_layout);
        avindicatorview = findViewById(R.id.avindicatorview);
        temp_layout = findViewById(R.id.temp_layout);
        try {
            uploadFilesToServer();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onViewDocumentClick(View view) {
        //Toast.makeText(this, "Document", Toast.LENGTH_SHORT).show();
        String newFilePath = GlobalVariables.signedDocumentFilePath;
        if (newFilePath!=null){
            File file = new File(newFilePath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            startActivity(intent);
        }
    }

    public void onWatchRecordingClick(View view) {
        //Toast.makeText(this, "Screen Recording", Toast.LENGTH_SHORT).show();
//        String newVideoPath = Utils.getRootPathOfApp(this)+"/1.mp4";
        String newVideoPath = GlobalVariables.screenRecordingVideoFilePath;
        if (newVideoPath!=null){
            File file = new File(newVideoPath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "video/*");
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
}
