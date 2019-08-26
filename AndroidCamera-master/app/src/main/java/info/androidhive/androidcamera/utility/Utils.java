package info.androidhive.androidcamera.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.androidhive.androidcamera.ApplicationConstants;
import info.androidhive.androidcamera.MainActivity;
import info.androidhive.androidcamera.R;
import info.androidhive.androidcamera.enums.ConnectionEnums;
import info.androidhive.androidcamera.interfaces.ProcessAfterCheckingInternetConnection;

public class Utils {

    public static Bitmap rescaleBitmapWidthHeight(Bitmap bitmap, int size){
        int width, height;
        if (bitmap.getWidth()>bitmap.getHeight()){
            width=size;
            height = (int)(width*((float)bitmap.getHeight()/(float)bitmap.getWidth()));
        } else{
            height = size;
            width = (int)(height*((float)bitmap.getWidth()/(float)bitmap.getHeight()));
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }


    private static final String TAG = "Utils";

    public static String storeImage(Bitmap image, Context context, int quality) {
        File pictureFile = getOutputMediaFile(context);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
            return null;
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(Context context){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
//                + "/Android/data/"
//                + context.getApplicationContext().getPackageName()
//                + "/Files");

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/" +context.getString(R.string.app_name)
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        String mImageName="IMG_"+ timeStamp +".jpeg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public static String getRootPathOfApp(Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/"
                + context.getString(R.string.app_name)
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        return mediaStorageDir.getAbsolutePath();
    }

    // InputStream -> File
    public static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try  {
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.flush();
            outputStream.close();

            // commons-io
            //IOUtils.copy(inputStream, outputStream);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void isInternetConnectionAvailable(final ProcessAfterCheckingInternetConnection processAfterCheckingInternetConnection, Context context){
        String url = ApplicationConstants.BASE_URL;
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        processAfterCheckingInternetConnection.processRequest(true, null);

                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error

                if (error instanceof NoConnectionError) {
                    processAfterCheckingInternetConnection.processRequest(false, ConnectionEnums.NO_INTERNET_CONNECTION);
                } else if (error instanceof TimeoutError){
                    processAfterCheckingInternetConnection.processRequest(false, ConnectionEnums.SERVER_DOWN);
                } else {
                    processAfterCheckingInternetConnection.processRequest(true, null);
                }

                error.printStackTrace();
            }
        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);
    }
}
