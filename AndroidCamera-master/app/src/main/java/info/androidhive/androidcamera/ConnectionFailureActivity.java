package info.androidhive.androidcamera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectionFailureActivity extends AppCompatActivity {
    ImageView imageview;
    TextView textview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_failure);
        imageview = findViewById(R.id.imageview);
        textview = findViewById(R.id.textview);

        switch (GlobalVariables.connectionEnums){
            case SERVER_DOWN:
                imageview.setImageResource(R.drawable.server);
                textview.setVisibility(View.VISIBLE);
                break;
            case NO_INTERNET_CONNECTION:
                imageview.setImageResource(R.drawable.no_i1);
                textview.setVisibility(View.GONE);
                break;
            default:
                imageview.setImageResource(R.drawable.server);
                textview.setVisibility(View.VISIBLE);
                break;
        }


    }

    public void onReload(View view) {
        startActivity(new Intent(this, LaunchingActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
