package info.androidhive.androidcamera.utility;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MergeVideo extends AsyncTask<Void, Integer, String> {


    private List<String> videoToMergeFilePaths;
    private String outputFilePath;
    private ProgressListener progressListener;

    public MergeVideo(List<String> videoToMergeFilePaths, String outputFilePath, ProgressListener progressListener){

        this.videoToMergeFilePaths = videoToMergeFilePaths;
        this.outputFilePath = outputFilePath;
        this.progressListener = progressListener;
    }

    @Override
    protected void onPreExecute() {

        // do initialization of required objects objects here
    };

    @Override
    protected String doInBackground(Void... params) {
        try {
            Movie[] inMovies = new Movie[videoToMergeFilePaths.size()];
            for (int i = 0; i < videoToMergeFilePaths.size(); i++) {

                inMovies[i] = MovieCreator.build(new FileDataSourceImpl(videoToMergeFilePaths.get(i)));

            }
//            List<Track> videoTracks = new LinkedList<Track>();
//            List<Track> audioTracks = new LinkedList<Track>();
//            for (Movie m : inMovies) {
//                for (Track t : m.getTracks()) {
//                    if (t.getHandler().equals("soun")) {
//                        audioTracks.add(t);
//                    }
//                    if (t.getHandler().equals("vide")) {
//                        videoTracks.add(t);
//                    }
//                }
//            }
//
//            Movie result = new Movie();
//
//            if (audioTracks.size() > 0) {
//                result.addTrack(new AppendTrack(audioTracks
//                        .toArray(new Track[audioTracks.size()])));
//            }
//            if (videoTracks.size() > 0) {
//                result.addTrack(new AppendTrack(videoTracks
//                        .toArray(new Track[videoTracks.size()])));
//            }

//            BasicContainer out = (BasicContainer) new DefaultMp4Builder()
//                    .build(result);
//
//            @SuppressWarnings("resource")
//
//
//            FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFilePath));
//            FileChannel fc = fileOutputStream.getChannel();
////          FileChannel fc = new RandomAccessFile(outputFilePath, "rw").getChannel();
//            out.writeContainer(fc);
//            fc.close();

            final Movie movieA = MovieCreator.build(new FileDataSourceImpl(videoToMergeFilePaths.get(0)));
            final Movie movieB = MovieCreator.build(new FileDataSourceImpl(videoToMergeFilePaths.get(1)));

            final Movie finalMovie = new Movie();

            final List<Track> movieOneTracks = movieA.getTracks();
            final List<Track> movieTwoTracks = movieB.getTracks();

            for (int i = 0; i < movieOneTracks.size() || i < movieTwoTracks.size(); ++i) {
                finalMovie.addTrack(new AppendTrack(movieTwoTracks.get(i), movieOneTracks.get(i)));
            }

            final Container container = new DefaultMp4Builder().build(finalMovie);

            final FileOutputStream fos = new FileOutputStream(new File(String.format(outputFilePath)));
            final WritableByteChannel bb = Channels.newChannel(fos);
            container.writeContainer(bb);
            fos.close();



        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            progressListener.onError(e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            progressListener.onError(e);
        }


        return outputFilePath;
    }

    @Override
    protected void onPostExecute(String value) {
        super.onPostExecute(value);
        progressListener.onFinish();
    }

    public interface ProgressListener {
        void onFinish();
        void onError(Throwable e);
    }
}

