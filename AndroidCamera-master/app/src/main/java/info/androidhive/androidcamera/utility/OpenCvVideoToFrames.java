package info.androidhive.androidcamera.utility;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

public class OpenCvVideoToFrames {
    private static String rootPathInput = "/Users/abhishek/Documents/transbit/video_test/input/";
	private static String rootPathOutput = "//Users//abhishek//Documents//transbit//video_test//output//";
	private static String rootPathOutputVideo = "/Users/abhishek/Documents/transbit/video_test/output_video/";

	private static ArrayList<Mat> listOfMat = new ArrayList<>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		String fileName = "5.mp4";
		String filePath = rootPathInput+fileName;
		convertVideoToFrames(filePath, rootPathOutputVideo+fileName);



	}

	static{ System.loadLibrary("opencv_java3"); }

	public static void convertVideoToFrames(String inputFilePath, String outputFilePath){
		long startingTime = System.currentTimeMillis();
		System.out.println("Video to frames conversion.");

		VideoCapture videoObj = new VideoCapture(inputFilePath);
		System.out.println("Video loaded.");
		int count=0;
		Mat frame = new Mat();
		listOfMat = new ArrayList<>();
		while(true){
			if (videoObj.read(frame)) {
				//Mat destination = new Mat();
				// Converting the image to gray scale and
		        // saving it in the dst matrix
		        //Imgproc.cvtColor(frame, destination, Imgproc.COLO);

		        //Imgproc.threshold(destination, destination, 20, 255, Imgproc.THRESH_BINARY);
		        listOfMat.add(frame);
		        frame = new Mat();
		     // Writing the image
		        count++;
		        //String outputFilePath = rootPathOutput+count+".jpg";
		        //Imgcodecs.imwrite(outputFilePath, destination);
			} else {
				break;
			}
		}
		videoObj.release();
		long timeTaken = (System.currentTimeMillis()-startingTime)/1000;
		System.out.println("Successfully converted. Time taken = "+timeTaken+" seconds");
		convertImagesToVideo(outputFilePath);
	}

	private static void convertImagesToVideo(String outputVideoFilePath){
		System.out.println("Frames to video conversion started");
		long startingTime = System.currentTimeMillis();

		File file = new File(outputVideoFilePath);
		if (file.exists()) {
			file.delete();
		}
		VideoWriter videoWriter = new VideoWriter(outputVideoFilePath, VideoWriter.fourcc('M', 'P', '4', '2'), 24, new Size(listOfMat.get(listOfMat.size()-1).width(), listOfMat.get(listOfMat.size()-1).height()));
		//3IV0 is the codec for MPEG4
		for (Mat mat: listOfMat){

			videoWriter.write(mat);
		}
		videoWriter.release();
		long timeTaken = (System.currentTimeMillis()-startingTime)/1000;
		System.out.println("Successfully converted. Time taken = "+timeTaken+" seconds");
	}

}