//package info.androidhive.androidcamera.video_recording;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.Configuration;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.MediaRecorder;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.support.annotation.NonNull;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.Surface;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//
//import info.androidhive.androidcamera.R;
//
//public class VideoCamera {
//    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
//    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
//    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
//    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
//    private Activity activity;
//
//    private static final String TAG = "Camera2VideoFragment";
//    // private static final int REQUEST_VIDEO_PERMISSIONS = 1;
//    // private static final String FRAGMENT_DIALOG = "dialog";
//
////    private static final String[] VIDEO_PERMISSIONS = {
////            Manifest.permission.CAMERA,
////            Manifest.permission.RECORD_AUDIO,
////    };
//
//
//    static {
//        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//    static {
//        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
//        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
//        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
//        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
//    }
//
//    /**
//     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
//     */
//    private CameraDevice mCameraDevice;
//
//    /**
//     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
//     * preview.
//     */
//    private CameraCaptureSession mPreviewSession;
//
//    /**
//     * The {@link android.util.Size} of camera preview.
//     */
//    private Size mPreviewSize;
//
//    /**
//     * The {@link android.util.Size} of video recording.
//     */
//    private Size mVideoSize;
//
//    /**
//     * MediaRecorder
//     */
//    private MediaRecorder mMediaRecorder;
//
//    /**
//     * Whether the app is recording video now
//     */
//    private boolean mIsRecordingVideo;
//
//    /**
//     * An additional thread for running tasks that shouldn't block the UI.
//     */
//    private HandlerThread mBackgroundThread;
//
//    /**
//     * A {@link Handler} for running tasks in the background.
//     */
//    private Handler mBackgroundHandler;
//
//    /**
//     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
//     */
//    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
//
//    /**
//     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
//     */
//    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
//
//        @Override
//        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            mCameraDevice = cameraDevice;
//            startPreview();
//            mCameraOpenCloseLock.release();
//
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            mCameraOpenCloseLock.release();
//            cameraDevice.close();
//            mCameraDevice = null;
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice cameraDevice, int error) {
//            mCameraOpenCloseLock.release();
//            cameraDevice.close();
//            mCameraDevice = null;
//
//            if (null != activity) {
//                activity.finish();
//            }
//        }
//
//    };
//    private Integer mSensorOrientation;
//    private String mNextVideoAbsolutePath;
//    private CaptureRequest.Builder mPreviewBuilder;
//
//    public VideoCamera newInstance() {
//        return new VideoCamera();
//    }
//
//    public Activity getActivity() {
//        return activity;
//    }
//
//    public void setActivity(Activity activity) {
//        this.activity = activity;
//    }
//
//    /**
//     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
//     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
//     *
//     * @param choices The list of available sizes
//     * @return The video size
//     */
//    private static Size chooseVideoSize(Size[] choices) {
//        for (Size size : choices) {
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
//        }
//        Log.e(TAG, "Couldn't find any suitable video size");
//        return choices[choices.length - 1];
//    }
//
//    /**
//     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
//     */
//    @SuppressWarnings("MissingPermission")
//    private void openCamera(int width, int height) {
////        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
////            requestVideoPermissions();
////            return;
////        }
//        final Activity activity = getActivity();
//        if (null == activity || activity.isFinishing()) {
//            return;
//        }
//        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
//        try {
//            Log.d(TAG, "tryAcquire");
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
//            }
//            String cameraId = manager.getCameraIdList()[0];
//
//            // Choose the sizes for camera preview and video recording
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics
//                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//            if (map == null) {
//                throw new RuntimeException("Cannot get available preview/video sizes");
//            }
//            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
//            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                    width, height, mVideoSize);
//
//            int orientation = getActivity().getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                //mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            } else {
//                //mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            }
//            //configureTransform(width, height);
//            mMediaRecorder = new MediaRecorder();
//            manager.openCamera(cameraId, mStateCallback, null);
//        } catch (CameraAccessException e) {
//            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
//            activity.finish();
//        } catch (NullPointerException e) {
//            // Currently an NPE is thrown when the Camera2API is used but not supported on the
//            // device this code runs.
////            ErrorDialog.newInstance(getString(R.string.camera_error))
////                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
//            Log.e("", activity.getString(R.string.camera_error));
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Interrupted while trying to lock camera opening.");
//        }
//    }
//
//    /**
//     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
//     * width and height are at least as large as the respective requested values, and whose aspect
//     * ratio matches with the specified value.
//     *
//     * @param choices     The list of sizes that the camera supports for the intended output class
//     * @param width       The minimum desired width
//     * @param height      The minimum desired height
//     * @param aspectRatio The aspect ratio
//     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
//     */
//    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            if (option.getHeight() == option.getWidth() * h / w &&
//                    option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
//        }
//
//        // Pick the smallest of those, assuming we found any
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new Camera2VideoFragment.CompareSizesByArea());
//        } else {
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            return choices[0];
//        }
//    }
//
//}
