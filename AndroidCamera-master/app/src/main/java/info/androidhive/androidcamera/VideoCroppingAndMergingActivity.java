package info.androidhive.androidcamera;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.wang.avi.AVLoadingIndicatorView;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import info.androidhive.androidcamera.utility.MergeVideo;
import info.androidhive.androidcamera.utility.Utils;
import processing.ffmpeg.videokit.AsyncCommandExecutor;
import processing.ffmpeg.videokit.Command;
import processing.ffmpeg.videokit.ProcessingListener;
import processing.ffmpeg.videokit.VideoKit;

public class VideoCroppingAndMergingActivity extends AppCompatActivity {
    AVLoadingIndicatorView avindicatorview;

    static{ System.loadLibrary("opencv_java3"); }

    private FFmpeg ffmpeg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_cropping_and_merging);
        avindicatorview = findViewById(R.id.avindicatorview);
        avindicatorview.show();
        loadFFMpegBinary();
       // convertVideoToFrames(getInputFilePath1(), getOutPutFilePath());
        String[] cutVideoCommand = { "-y", "-i", getInputFilePath1(),"-ss", "" + 2000 / 1000, "-t", "" + 10000 / 1000, "-c","copy", getOutPutFilePath()};
        String concatenateCommandWithoutAudio[] = {"-i",getInputFilePath1(),"-i",getInputFilePath2(),"-i",getInputFilePath3(),"-filter_complex","[0:0] [1:0] [2:0] concat=n=3:v=1:a=0",getOutPutFilePath()};
        //-filter_complex '[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]'
        String concatenateCommand[] = {"-i",getInputFilePath1(),"-i",getInputFilePath2() ,"-filter_complex","'[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]'","-map","[vid]","-c:v", "libx264","-crf", "23", "-preset", "veryfast", getOutPutFilePath()};
        //concatenateVideos();
        //concatenate(getInputFilePath1(), getInputFilePath2(), getOutPutFilePath());
        //execFFmpegCommand(concatenateCommand);
        mergeVideos();
    }
    
    private void mergeVideos(){
        List<String> videoToMergeFilePaths = new ArrayList<>();
        videoToMergeFilePaths.add(getInputFilePath1());
        videoToMergeFilePaths.add(getInputFilePath2());
        //videoToMergeFilePaths.add(getInputFilePath3());
        new MergeVideo(videoToMergeFilePaths, getOutPutFilePath(), new MergeVideo.ProgressListener() {
            @Override
            public void onFinish() {
                Toast.makeText(VideoCroppingAndMergingActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(VideoCroppingAndMergingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {

                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                }

                @Override
                public void onSuccess() {
                    Log.e("FFMPEG", "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {

        } catch (Exception e) {

        }
    }


    private void execFFmpegCommand(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.e("FFMPEG", "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.e("FFMPEG", "SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.e("FFMPEG", "Started command : ffmpeg " + command);
                    Log.e("FFMPEG", "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.e("FFMPEG", "Started command : ffmpeg " + command);

                }

                @Override
                public void onFinish() {
                    Log.e("FFMPEG", "Finished command : ffmpeg " + command);



                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e("FFMPEG", "Error command : ffmpeg " + command);
            // do nothing for now
        }
    }










    private String getOutPutFilePath(){
        //String filePathForScreenRecordingVideo = Utils.getRootPathOfApp(getApplicationContext())+"/"+getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE)+"_"+getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER)+"_"+ Utils.getCurrentDateAndTime()+"_sr_c"+".mp4";
        return Utils.getRootPathOfApp(getApplicationContext())+"/9999.mp4";
    }

    private String getInputFilePath1(){
        return Utils.getRootPathOfApp(getApplicationContext())+"/"+"1.mp4";
        //return GlobalVariables.screenRecordingVideoFilePath;
    }

    private String getInputFilePath2(){
        return Utils.getRootPathOfApp(getApplicationContext())+"/"+"2.mp4";
        //return GlobalVariables.screenRecordingVideoFilePath;
    }

    private String getInputFilePath3(){
        return Utils.getRootPathOfApp(getApplicationContext())+"/"+"3.mp4";
        //return GlobalVariables.screenRecordingVideoFilePath;
    }


    private void startCompleteSessionActivityAndFinishActivity(){
        Intent intent = new Intent(this, CompleteSessionActivity.class);
        intent.putExtra(ApplicationConstants.COUNTRY_CODE, getIntent().getStringExtra(ApplicationConstants.COUNTRY_CODE));
        intent.putExtra(ApplicationConstants.MOBILE_NUMBER, getIntent().getStringExtra(ApplicationConstants.MOBILE_NUMBER));
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {

    }

    public void concatenate(String inputFile1, String inputFile2, String outputFile) {
        Log.d("", "Concatenating " + inputFile1 + " and " + inputFile2 + " to " + outputFile);
        String list = generateList(new String[] {inputFile1, inputFile2});
        String commandString = "ffmpeg" + " " +"-f" + " "+"concat"+ " "+"-i"+" "+list + " "+ "-c"+ " " +"copy"+ " "+outputFile;
        final VideoKit videoKit = new VideoKit();
        final Command command = videoKit.createCommand()
                .overwriteOutput()
                .inputPath(getInputFilePath1())
                .inputPath(getInputFilePath2())
                .outputPath(getOutPutFilePath())
                .customCommand(commandString)
                .copyVideoCodec()
                .experimentalFlag()
                .build();



        new AsyncCommandExecutor(command, new ProcessingListener() {
            @Override
            public void onSuccess(String path) {
                //Toast.makeText(VideoCroppingAndMergingActivity.this, "success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int returnCode) {
               // Toast.makeText(VideoCroppingAndMergingActivity.this, "failure", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    /**
     * Generate an ffmpeg file list
     * @param inputs Input files for ffmpeg
     * @return File path
     */
    private static String generateList(String[] inputs) {
        File list;
        Writer writer = null;
        try {
            list = File.createTempFile("ffmpeg-list", ".txt");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (String input: inputs) {
                writer.write("file '" + input + "'\n");
                Log.d("", "Writing to list file: file '" + input + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Log.d("", "Wrote list file to " + list.getAbsolutePath());
        return list.getAbsolutePath();
    }


    public void concatenateVideos(){
        String []complexCommand = new String[]{"-y", "-i", getInputFilePath1(), "-i", getInputFilePath2(), "-strict", "experimental", "-filter_complex",
                "[0:v]scale=480x640,setsar=1:1[v0];[1:v]scale=480x640,setsar=1:1[v1];[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
                "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "480x640", "-vcodec", "libx264","-crf","24","-q","4","-preset", "ultrafast", getOutPutFilePath()};
        execFFmpegCommand(complexCommand);
    }
}
