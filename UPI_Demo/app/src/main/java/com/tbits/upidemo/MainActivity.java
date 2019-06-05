package com.tbits.upidemo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUpiId, editTextAmount, editTextName, editTextNote;

    private String TAG = "MainActivity";
    String payeeAddress=""; //payee UPI id.
    String payeeName="";
    String transactionNote="";
    String amount="";
    String currencyUnit = "INR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button btnSubmit = (Button) findViewById(R.id.submit);

        editTextAmount = (EditText) findViewById(R.id.editTextAmount);
        editTextUpiId = (EditText) findViewById(R.id.editTextUpiId);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextNote = (EditText) findViewById(R.id.editTextNote);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payeeName = editTextName.getText().toString();
                payeeAddress = editTextUpiId.getText().toString();

                DecimalFormat numberFormat = new DecimalFormat("0.00");
                if (!editTextAmount.getText().toString().isEmpty())
                    amount = numberFormat.format(Double.parseDouble(editTextAmount.getText().toString()));

                transactionNote = editTextNote.getText().toString();

                if (payeeAddress.trim().isEmpty() || payeeName.trim().isEmpty() || amount.trim().isEmpty()){
                    Toast.makeText(MainActivity.this, "Empty mandatory fields", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, amount, Toast.LENGTH_SHORT).show();
                } else {
                    Uri uri = Uri.parse("upi://pay?pa="+payeeAddress+"&pn="+payeeName+"&tn="+transactionNote+
                            "&am="+amount+"&cu="+currencyUnit);
                    Log.d(TAG, "onClick: uri: "+uri);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivityForResult(intent,1);
                    } catch (Throwable e){
                        if (e instanceof ActivityNotFoundException) {
                            Toast.makeText(MainActivity.this, "UPI app may not be present.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                }


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult: requestCode: "+requestCode);
        Log.d(TAG, "onActivityResult: resultCode: "+resultCode);
        //txnId=UPI20b6226edaef4c139ed7cc38710095a3&responseCode=00&ApprovalRefNo=null&Status=SUCCESS&txnRef=undefined
        //txnId=UPI608f070ee644467aa78d1ccf5c9ce39b&responseCode=ZM&ApprovalRefNo=null&Status=FAILURE&txnRef=undefined

        if(data!=null) {
            Log.d(TAG, "onActivityResult: data: " + data.getStringExtra("response"));
            String res = data.getStringExtra("response");
            String search = "SUCCESS";
            if (res.toLowerCase().contains(search.toLowerCase())) {
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
