package info.androidhive.androidcamera;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import com.rilixtech.CountryCodePicker;

public class MobileNumberGetActivity extends AppCompatActivity {
    private AppCompatEditText edtPhoneNumber;
    private CountryCodePicker ccp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number_get);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = (AppCompatEditText) findViewById(R.id.phone_number_edt);
        ccp.registerPhoneNumberTextView(edtPhoneNumber);


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
        displayMessageForTheMobileNumberPresence();
    }
}
