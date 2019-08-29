package info.androidhive.androidcamera;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.File;

import info.androidhive.androidcamera.enums.ConnectionEnums;
import info.androidhive.androidcamera.face_tracking.FaceTrackerActivity;
import info.androidhive.androidcamera.interfaces.ProcessAfterCheckingInternetConnection;
import info.androidhive.androidcamera.utility.Utils;


public class LaunchingActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 11;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 12;
    private final int MY_PERMISSIONS_REQUEST_INTERNET = 13;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 14;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 15;
    private final int MY_PERMISSIONS_REQUEST_READ_GESERVICES = 16;
    private final int MY_PERMISSIONS_REQUEST_READ_WRITE_RECEIVE_SMS = 17;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 18;
    private final int MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 19;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION = 20;



    private RelativeLayout rootLayout;
    private AVLoadingIndicatorView avindicatorview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);
        rootLayout = findViewById(R.id.root_layout);
        rootLayout.setVisibility(View.INVISIBLE);
        avindicatorview = findViewById(R.id.avindicatorview);

        avindicatorview.show();
        checkPermissions();

    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,  Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_RECEIVE_SMS);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION);
        }
        else {
            checkInternetConnection();
            //uploadTest();
        }

    }

    private void uploadTest(){

    }

    private void checkInternetConnection(){
        rootLayout.setVisibility(View.VISIBLE);


        Utils.isInternetConnectionAvailable(new ProcessAfterCheckingInternetConnection() {
            @Override
            public void processRequest(boolean connectionStatus, ConnectionEnums connectionEnums) {
                if (connectionStatus){
                    startAction();
                } else {
                    GlobalVariables.connectionEnums = connectionEnums;
                    switch (connectionEnums){
                        case NO_INTERNET_CONNECTION:
                            Toast.makeText(LaunchingActivity.this, "no internet connection", Toast.LENGTH_LONG).show();
                            break;
                        case SERVER_DOWN:
                            Toast.makeText(LaunchingActivity.this, "unable to connect to server", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(LaunchingActivity.this, "some unknown problem while connection", Toast.LENGTH_LONG).show();

                    }
                    startActivity(new Intent(LaunchingActivity.this, ConnectionFailureActivity.class));
                    LaunchingActivity.this.finish();
                }
            }
        }, getApplicationContext());
    }

    private void startAction(){

        //configureSettings();
//        readSms();
        startActivityForResult(new Intent(LaunchingActivity.this,
                        MobileNumberGetActivity.class),
                ApplicationConstants.REQUEST_CODE_FOR_MAIN_APPLICATION);
    }

    private void configureSettings(){


    }

    public void readSms(){

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c = getContentResolver().query(uri, null, null ,null,null);
        startManagingCursor(c);
        int num = c.getCount();
        // Read the sms data
        if(c.moveToFirst()) {
            for(int i = 0; i < c.getCount()-(c.getCount()-10); i++) {

                String mobile = c.getString(c.getColumnIndexOrThrow("address")).toString();
                String message = c.getString(c.getColumnIndexOrThrow("body")).toString();


                c.moveToNext();
            }

        }
        c.close();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ApplicationConstants.REQUEST_CODE_FOR_MAIN_APPLICATION){
            this.setResult(resultCode);

        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            } case MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_WRITE_RECEIVE_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Toast.makeText(this, "permission needed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
}
