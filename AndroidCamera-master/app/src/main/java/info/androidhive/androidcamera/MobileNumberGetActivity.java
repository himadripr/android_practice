package info.androidhive.androidcamera;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rilixtech.Country;
import com.rilixtech.CountryCodePicker;

public class MobileNumberGetActivity extends AppCompatActivity {
    private AppCompatEditText invisible_mobile_number_hint;
    private EditText edtPhoneNumber;
    String hint;
    private CountryCodePicker ccp;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number_get);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = (EditText) findViewById(R.id.phone_number_edt);
        invisible_mobile_number_hint = (AppCompatEditText) findViewById(R.id.invisible_mobile_number_hint);
        ccp.registerPhoneNumberTextView(invisible_mobile_number_hint);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {

                MyCountDownTimer myCountDownTimer = new MyCountDownTimer(200, 100);
                myCountDownTimer.start();
                progressDialog = ProgressDialog.show(MobileNumberGetActivity.this, "", "");
            }
        });

    }

    private String removeSpcaesAndFirst0FromTheMobileNumberHint(String string){
        if (string.charAt(0) == '('){
            string = string.substring(1);
        }
        if (string.charAt(0) == '0'){
            string = string.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        for (int j=0;j<string.length();j++){
            if (string.charAt(j) != ' ' && string.charAt(j) != '-' && string.charAt(j) != ')' && string.charAt(j) != '('){
                sb.append(string.charAt(j));
            }
        }
        return sb.toString();
    }

    private void displayMessageForTheMobileNumberPresence(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important");
        builder.setMessage("Do you have the mobile number "+edtPhoneNumber.getText().toString()+" present in this handset because the app will do the auto otp verification.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void startActivity(){
        Intent intent = new Intent(this, OtpSendAndVerificationActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, ccp.getSelectedCountryCode());
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, edtPhoneNumber.getText().toString());
        startActivityForResult(intent, MainActivity.REQUEST_CODE_CAPTURE_PERM);
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
        String hint = edtPhoneNumber.getHint().toString();
        String mobileNumber = edtPhoneNumber.getText().toString();
        if (hint.length() == mobileNumber.length()){
            displayMessageForTheMobileNumberPresence();
        } else {
            Toast.makeText(this, "Incorrect length of mobile number", Toast.LENGTH_SHORT).show();
        }

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
            hint = invisible_mobile_number_hint.getHint().toString();
            hint = removeSpcaesAndFirst0FromTheMobileNumberHint(hint);
            edtPhoneNumber.setHint(hint);
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
}
