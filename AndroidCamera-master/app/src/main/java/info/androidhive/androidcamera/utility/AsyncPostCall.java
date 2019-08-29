package info.androidhive.androidcamera.utility;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;

import info.androidhive.androidcamera.ApplicationConstants;
import info.androidhive.androidcamera.GlobalVariables;
import info.androidhive.androidcamera.face_tracking.FaceTrackerActivity;
import info.androidhive.androidcamera.interfaces.PostCallResponseHandler;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncPostCall extends AsyncTask<Void, Void, Boolean> {
    WeakReference<Activity> activityWeakReference;
    ArrayList<Exception> exceptions;
    PostCallResponseHandler postCallResponseHandler;

    public AsyncPostCall(Activity activity, PostCallResponseHandler postCallResponseHandler){
        activityWeakReference = new WeakReference<Activity>(activity);
        exceptions = new ArrayList<>();
        this.postCallResponseHandler = postCallResponseHandler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String result = uploadData();
        downloadSignedFileForTheGivenMobileNumber();
        return true;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }

    public String uploadData() {

            try {

                final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
                final MediaType MEDIA_TYPE_MP4 = MediaType.parse("video/mp4");


                RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                        .addFormDataPart("userid", "8457851245")
//                        .addFormDataPart("userfile","profile.png", RequestBody.create(MEDIA_TYPE_JPG, file))

                        .addFormDataPart("screenRecording","screenRecording.mp4", RequestBody.create(MEDIA_TYPE_MP4, new File(GlobalVariables.screenRecordingVideoFilePath)))
                        .addFormDataPart("signature","signature.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.signatureImagePath)))
                        .addFormDataPart("startingImage","startingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.startingImageFilePath)))
                        .addFormDataPart("endingImage","endingImage.jpg", RequestBody.create(MEDIA_TYPE_JPG, new File(GlobalVariables.endingImageFilePath)))

                        .addFormDataPart("mobileNumber", GlobalVariables.mobileNumber)
                        .addFormDataPart("startingLatitude", GlobalVariables.startingLatitudes)
                        .addFormDataPart("startingLongitude", GlobalVariables.startingLongitudes)
                        .addFormDataPart("startingDateTime", GlobalVariables.startingTime)
                        .addFormDataPart("endingLatitude", GlobalVariables.endingLatitudes)
                        .addFormDataPart("endingLongitude", GlobalVariables.endingLongitudes)
                        .addFormDataPart("endingDateTime", GlobalVariables.endingTime)

                        .build();

                Request request = new Request.Builder()

                        .url(ApplicationConstants.BASE_URL+"/document/upload")

                        .post(req)

                        .build();

                OkHttpClient client;
                // When building OkHttpClient, the OkHttpClient.Builder() is passed to the with() method to initialize the configuration
//                client = ProgressManager.getInstance().with(new OkHttpClient.Builder())
//                        .build();

                client = new OkHttpClient.Builder().build();
                Response response = client.newCall(request).execute();

                Log.d("response", "uploadImage:"+response.body().string());
                String result = response.body().string();
//                return new JSONObject(response.body().string());
                return response.body().string();

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                exceptions.add(e);
                Log.e("", "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                exceptions.add(e);
                Log.e("", "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }

        private void downloadSignedFileForTheGivenMobileNumber(){

            String url = ApplicationConstants.BASE_URL+"/getSignedDocument/"+activityWeakReference.get().getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE)+"/"+
                    activityWeakReference.get().getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER);

            InputStreamVolleyRequest request = new InputStreamVolleyRequest(com.android.volley.Request.Method.GET, url,
                    new com.android.volley.Response.Listener<byte[]>() {
                        @Override
                        public void onResponse(byte[] response) {
                            // TODO handle the response

                            try {
                                if (response!=null) {
                                    FileOutputStream outputStream;
                                    String dirPath = Utils.getRootPathOfApp(activityWeakReference.get().getApplicationContext());
                                    String timeStamp = String.valueOf(System.currentTimeMillis());
                                    String name = dirPath+"/"+"Signed_"+timeStamp+".pdf";
                                    File file = new File(name);
                                    file.createNewFile();
                                    outputStream = new FileOutputStream(file);
                                    outputStream.write(response);
                                    outputStream.close();
                                    //Toast.makeText(FaceTrackerActivity.this, "Download complete.", Toast.LENGTH_SHORT).show();
                                    GlobalVariables.signedDocumentFilePath = file.getAbsolutePath();
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                               // Toast.makeText(FaceTrackerActivity.this, "Unable to download file", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    } ,new com.android.volley.Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO handle the error

                    //Toast.makeText(FaceTrackerActivity.this, "Unable to download file", Toast.LENGTH_SHORT).show();

                    error.printStackTrace();
                }
            }, null);
            RequestQueue mRequestQueue = Volley.newRequestQueue(activityWeakReference.get().getApplicationContext(), new HurlStack());
            mRequestQueue.add(request);

        }


        @Override
        protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);

            if (flag){
                Toast.makeText(activityWeakReference.get(), "Process completed...", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(activityWeakReference.get(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                for (Exception e: exceptions){
                    Toast.makeText(activityWeakReference.get(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            postCallResponseHandler.processResponse(null);

        }
    }