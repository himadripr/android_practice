package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.msg91.sendotpandroid.library.SendOtpVerification;

import com.msg91.sendotpandroid.library.Verification;
import com.msg91.sendotpandroid.library.VerificationListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpSendAndVerificationActivity extends AppCompatActivity implements VerificationListener{
    private String mobileNumber;
    private String countryCode;
    private TextView textView;
    private ProgressDialog progressDialog;
    private Verification mVerification;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
                Pattern pattern = Pattern.compile("[0-9][0-9][0-9][0-9]");
                Matcher matcher = pattern.matcher(message);

                if (matcher.find()){
                    String otp = matcher.group();
                    EditText editText = findViewById(R.id.ed_otp);
                    editText.setText(otp);
                    mVerification.verify(otp);
                }


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        mobileNumber = getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER);
        countryCode  = getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE);
        textView = findViewById(R.id.textview);
        String fullNumber = " +"+countryCode+mobileNumber;
        textView.setText(new StringBuilder().append(textView.getText().toString()).append(fullNumber).toString());
        //Toast.makeText(this, "mobile number: "+mobileNumber + " , country code: "+countryCode, Toast.LENGTH_SHORT).show();
        sendOtpToMobileNumber(fullNumber.substring(1));
    }

    private void sendOtpToMobileNumber(String mobileNumber){
        mVerification = SendOtpVerification.createSmsVerification
                (SendOtpVerification
                        .config(mobileNumber)
                        .context(this)
                        .autoVerification(false)
                        .httpsConnection(false)//connection to be use in network calls
                        .expiry("30")//value in minutes
                        .senderId("TBITSR") //where XXXX is any string
                        .otplength("4") //length of your otp max length up to 9 digits
                        //--------case 1-------------------
                        .message("Your OTP code is ##OTP##. It will expire in 30 minutes.")//##OTP## use for default generated OTP
                        //--------case 2-------------------
                        //-------------------------------------
                        //use single case at a time either 1 or 2
                        .build(), this);
        mVerification.initiate();
        progressDialog = ProgressDialog.show(this, "", "");
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.REQUEST_CODE_CAPTURE_PERM){
            this.setResult(resultCode);

        }
        finish();
    }

    public void onProceed(View view) {
        startActivity();
    }

    private void startActivity(){
        startActivityForResult(new Intent(this,
                        MainActivity.class),
                MainActivity.REQUEST_CODE_CAPTURE_PERM);
    }

    /**
     *
     * @param response
     */

    @Override
    public void onInitiated(String response) {
        progressDialog.dismiss();
    }

    @Override
    public void onInitiationFailed(Exception paramException) {
        System.out.println(paramException.getMessage());

        Toast.makeText(this, paramException.getMessage(), Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();

    }

    @Override
    public void onVerified(String response) {
        progressDialog.dismiss();
        startActivity();
    }

    @Override
    public void onVerificationFailed(Exception paramException) {
        progressDialog.dismiss();
        Toast.makeText(this, "Invalid otp", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(this,
                        MobileNumberGetActivity.class),
                MainActivity.REQUEST_CODE_CAPTURE_PERM);
    }
}
