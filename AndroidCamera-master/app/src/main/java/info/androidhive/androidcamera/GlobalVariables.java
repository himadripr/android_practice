package info.androidhive.androidcamera;

import android.media.MediaRecorder;

import java.util.ArrayList;

import info.androidhive.androidcamera.enums.ConnectionEnums;

public class GlobalVariables {
    public static String startingLatitudes;
    public static String startingLongitudes;
    public static String endingLatitudes;
    public static String endingLongitudes;
    public static String signatureImagePath;
    public static String startingTime;
    public static String endingTime;
    public static String screenRecordingVideoFilePath;
    public static ArrayList<Long> screenRecordingTimesInMillisisecondForCropping = new ArrayList<>(); //even index is starting time and odd index is ending.

    public static String signedDocumentFilePath;
    public static String startingImageFilePath;
    public static String endingImageFilePath;
    public static String mobileNumber;
    public static ConnectionEnums connectionEnums;
    public static MediaRecorder mediaRecorder;

}
