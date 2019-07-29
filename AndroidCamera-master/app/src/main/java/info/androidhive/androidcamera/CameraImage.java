package info.androidhive.androidcamera;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class CameraImage extends Fragment {
    private Camera camera;
    private boolean isFirstTime = true;
    private SurfaceViewWithOverlay surfaceViewWithOverlay;
    private SurfaceHolder previewSurfaceHolder;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT; //0 is for back camera
    // Actual preview size and orientation
    private Camera.Size cameraPreviewSize;
    ///////////////////////////////////////////////////////////////////////////////
    // Some application settings that can be changed to modify application behavior:
    // The camera zoom. Optically zooming with a good camera often improves results
    // even at close range and it is required at longer ranges.
    private static final int cameraZoom = 1;
    // Continuous autofocus is sometimes a problem. You can disable it if it is, or if you want
    // to experiment with a different approach (starting recognition in autofocus callback)
    private static final boolean disableContinuousAutofocus = false;

    private int orientation;

    // Auxiliary variables
    private boolean inPreview = false; // Camera preview is started
    private boolean stableResultHasBeenReached; // Stable result has been reached
    private boolean startRecognitionWhenReady; // Start recognition next time when ready (and reset this flag)
    private Handler handler = new Handler(); // Posting some delayed actions;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.camera_image,
                container, false);

        return rootView;

    }

    @Override
    public void onResume() {

        super.onResume();
        if (isFirstTime){
            RelativeLayout layout = (RelativeLayout) getView().findViewById(R.id.relativelayout);
            surfaceViewWithOverlay = new SurfaceViewWithOverlay( getActivity() );
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT );
            surfaceViewWithOverlay.setLayoutParams( params );
            // Add the surface to the layout as the bottom-most view filling the parent
            layout.addView( surfaceViewWithOverlay, 0 );

            surfaceViewWithOverlay.getHolder().addCallback( surfaceCallback );
            isFirstTime = false;
        }
        clearRecognitionResults();
        camera = Camera.open(cameraId);

        resetWindowFrameAndCameraSettings();
    }

    public void resetWindowFrameAndCameraSettings() {
        if (previewSurfaceHolder!=null)
            setCameraPreviewDisplayAndStartPreview();
    }

    @Override
    public void onPause()
    {
        handler.removeCallbacksAndMessages( null );
        clearRecognitionResults();
        stopPreviewAndReleaseCamera();
        super.onPause();

    }


    // This callback is used to configure preview surface for the camera
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated( SurfaceHolder holder )
        {
            // When surface is created, store the holder
            previewSurfaceHolder = holder;
            //setCameraPreviewDisplayAndStartPreview();
        }

        @Override
        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
        {
            // When surface is changed (or created), attach it to the camera, configure camera and start preview
            if( camera != null ) {
                setCameraPreviewDisplayAndStartPreview();
            }
        }

        @Override
        public void surfaceDestroyed( SurfaceHolder holder )
        {
            // When surface is destroyed, clear previewSurfaceHolder
            previewSurfaceHolder = null;
        }
    };

    // Start recognition when autofocus completes (used when continuous autofocus is disabled)
    private Camera.AutoFocusCallback captureImageCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus( boolean success, Camera camera )
        {
            if (success){
                //captureImageUtil();
            } else {
                autoFocus(captureImageCameraAutoFocusCallback);
            }
        }
    };

    // Enable 'Start' button when autofocus completes (used when continuous autofocus is disabled)
    private Camera.AutoFocusCallback enableCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus( boolean success, Camera camera )
        {

        }
    };

    // Start autofocus (used when continuous autofocus is disabled)
    private void autoFocus( Camera.AutoFocusCallback callback )
    {
        if( camera != null ) {
            try {
                camera.autoFocus( callback );
            } catch( Exception e ) {
                Log.e( getString( R.string.app_name ), "Error: " + e.getMessage() );
            }
        }
    }

    // This callback will be used to obtain frames from the camera
    private Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame( byte[] data, Camera camera )
        {

        }

    };

    // Attach the camera to the surface holder, configure the camera and start preview
    private void setCameraPreviewDisplayAndStartPreview()
    {
        try {
            camera.setPreviewDisplay( previewSurfaceHolder );
        } catch( Throwable t ) {
            Log.e( getString( R.string.app_name ), "Exception in setPreviewDisplay()", t );
        }
        configureCameraAndStartPreview( camera );
    }

    // Stop preview and release the camera
    private void stopPreviewAndReleaseCamera()
    {
        if( camera != null ) {
            camera.setPreviewCallbackWithBuffer( null );
            if( inPreview ) {
                camera.stopPreview();
                inPreview = false;
            }
            camera.release();
            camera = null;
        }
    }

    // Clear recognition results
    void clearRecognitionResults()
    {
        stableResultHasBeenReached = false;

        // surfaceViewWithOverlay.setLines( null, ITextCaptureService.ResultStabilityStatus.NotReady );
        surfaceViewWithOverlay.setFillBackground( false );
    }

    private void configureCameraAndStartPreview( Camera camera )
    {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);


        // Configure camera parameters
        Camera.Parameters parameters = camera.getParameters();

        parameters.setRotation(result);

        int preferedHeight = surfaceViewWithOverlay.getHeight();
        // Select preview size. The preferred size is 1080x720 or just below this
        cameraPreviewSize = null;
        for( Camera.Size size : parameters.getSupportedPreviewSizes() ) {
            if( size.height <= preferedHeight || size.width <= preferedHeight ) {
                if( cameraPreviewSize == null ) {
                    cameraPreviewSize = size;
                } else {
                    int resultArea = cameraPreviewSize.width * cameraPreviewSize.height;
                    int newArea = size.width * size.height;
                    if( newArea > resultArea ) {
                        cameraPreviewSize = size;
                    }
                }
            }
        }
        parameters.setPreviewSize( cameraPreviewSize.width, cameraPreviewSize.height );
        int defaultSize = 300;
        int ratio = cameraPreviewSize.height/defaultSize;
        int pagerWidth;
        if (ratio==0){
            pagerWidth = defaultSize;
        } else{
           pagerWidth = cameraPreviewSize.width/ratio;
        }
        ViewPager viewPagerCamera = getActivity().findViewById(R.id.viewpagercamera);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                pagerWidth,
                defaultSize );
        params.gravity = Gravity.CENTER;
        viewPagerCamera.setLayoutParams(params);
        // Zoom
        parameters.setZoom( cameraZoom );

        // Buffer format. The only currently supported format is NV21
        parameters.setPreviewFormat( ImageFormat.NV21 );
        // Default focus mode
        parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO ); //earlier it was FOCUS_MODE_AUTO

//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        // Done
        try{
            camera.setParameters( parameters );
        } catch (Exception e){

        }


        // The camera will fill the buffers with image data and notify us through the callback.
        // The buffers will be sent to camera on requests from text capture service (see implementation
        // of ITextCaptureService.Callback.onRequestLatestFrame above)
        camera.setPreviewCallbackWithBuffer( cameraPreviewCallback );
        // Configure the view scale and area of interest (camera sees it as rotated 90 degrees, so
        // there's some confusion with what is width and what is height)
        surfaceViewWithOverlay.setScaleX( surfaceViewWithOverlay.getWidth(), cameraPreviewSize.height );
        surfaceViewWithOverlay.setScaleY( surfaceViewWithOverlay.getHeight(), cameraPreviewSize.width );


        int marginWidth=0;
        int marginHeight=0;
        surfaceViewWithOverlay.setAreaOfInterest(
                    new Rect( marginWidth, marginHeight, cameraPreviewSize.height - marginWidth,
                            cameraPreviewSize.width - marginHeight ) );



        // Start preview
        camera.startPreview();

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for(int i=0;i<sizes.size();i++)
        {
            if(sizes.get(i).width > size.width)
                size = sizes.get(i);
        }
        parameters.setPictureSize(size.width, size.height);
        try {
            camera.setParameters(parameters);
        } catch (Exception e){

        }

        // Choosing autofocus or continuous focus and whether to start recognition immediately or after autofocus or manually
        if( disableContinuousAutofocus ||
                !parameters.getSupportedFocusModes().contains( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) ) {
            // No continuous focus
            autoFocus(enableCameraAutoFocusCallback);

        } else {
            // Continuous focus. Have to use some Magic. Some devices expect preview to actually
            // start before enabling continuous focus has any effect. So we wait for the camera to
            // actually start preview
            handler.postDelayed( new Runnable() {
                public void run()
                {
                    Camera _camera = CameraImage.this.camera;
                    Camera.Parameters parameters = _camera.getParameters();
                    parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE );
                    try{
                        _camera.setParameters( parameters );
                    } catch (Exception e){

                    }

                    startRecognitionWhenReady = false;
                }
            }, 30 );
        }
        inPreview = true;

    }


    // Surface View combined with an overlay showing recognition results and 'progress'
    static class SurfaceViewWithOverlay extends SurfaceView {
        private Point[] quads;
        private String[] lines;
        private Rect areaOfInterest;
        private Rect faceAreaOfInterest;
        private int stability;
        private int scaleNominatorX = 1;
        private int scaleDenominatorX = 1;
        private int scaleNominatorY = 1;
        private int scaleDenominatorY = 1;
        private Paint textPaint;
        private Paint lineBoundariesPaint;
        private Paint backgroundPaint;
        private Paint areaOfInterestPaint;

        public SurfaceViewWithOverlay( Context context )
        {
            super( context );
            this.setWillNotDraw( false );

            lineBoundariesPaint = new Paint();
            lineBoundariesPaint.setStyle( Paint.Style.STROKE );
//            lineBoundariesPaint.setARGB( 255, 128, 128, 128 );
            lineBoundariesPaint.setARGB( 150, 0, 0, 0 );
            textPaint = new Paint();

            areaOfInterestPaint = new Paint();
//            areaOfInterestPaint.setARGB( 100, 0, 0, 0 );
            areaOfInterestPaint.setARGB( 150, 0, 0, 0 );
            areaOfInterestPaint.setStyle( Paint.Style.FILL );
        }

        public void setScaleX( int nominator, int denominator )
        {
            scaleNominatorX = nominator;
            scaleDenominatorX = denominator;
        }

        public void setScaleY( int nominator, int denominator )
        {
            scaleNominatorY = nominator;
            scaleDenominatorY = denominator;
        }

        public void setFillBackground( Boolean newValue )
        {
            if( newValue ) {
                backgroundPaint = new Paint();
                backgroundPaint.setStyle( Paint.Style.FILL );
//                backgroundPaint.setARGB( 100, 255, 255, 255 );
//                backgroundPaint.setARGB( 255, 255, 255, 255 );
                backgroundPaint.setARGB( 150, 0, 0, 0 );
            } else {
                backgroundPaint = null;
            }
            invalidate();
        }

        public void setAreaOfInterest( Rect newValue )
        {
            areaOfInterest = newValue;
            invalidate();
        }

        public Rect getFaceAreaOfInterest() {
            return faceAreaOfInterest;
        }

        public void setFaceAreaOfInterest(Rect faceAreaOfInterest) {
            this.faceAreaOfInterest = faceAreaOfInterest;
            invalidate();

        }

        public Rect getAreaOfInterest()
        {
            return areaOfInterest;
        }

        @Override
        protected void onDraw( Canvas canvas )
        {
            super.onDraw( canvas );
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.save();
            if( areaOfInterest != null ) {
                // Shading and clipping the area of interest
                int left, top, right, bottom;
//                if (isPortrait) {
                if (false) {
                    left = ( areaOfInterest.left * scaleNominatorX ) / scaleDenominatorX;
                    right = ( areaOfInterest.right * scaleNominatorX ) / scaleDenominatorX;
                    top = ( areaOfInterest.top * scaleNominatorY ) / scaleDenominatorY;
                    bottom = ( areaOfInterest.bottom * scaleNominatorY ) / scaleDenominatorY;
                } else  {
                    left =  areaOfInterest.left ;
                    right = areaOfInterest.right;
                    top = areaOfInterest.top;
                    bottom =  areaOfInterest.bottom ;
                }


                canvas.drawRect( 0, 0, width, top, areaOfInterestPaint );
                canvas.drawRect( 0, bottom, width, height, areaOfInterestPaint );
                canvas.drawRect( 0, top, left, bottom, areaOfInterestPaint );
                canvas.drawRect( right, top, width, bottom, areaOfInterestPaint );
                canvas.drawRect( left, top, right, bottom, lineBoundariesPaint );
                canvas.clipRect( left, top, right, bottom );
            }


            // If there is any result
            if( lines != null ) {
                // Shading (whitening) the background when stable
                if( backgroundPaint != null ) {
                    canvas.drawRect( 0, 0, width, height, backgroundPaint );
                }
                // Drawing the text lines
                for( int i = 0; i < lines.length; i++ ) {
                    // The boundaries
                    int j = 4 * i;
                    Path path = new Path();
                    Point p = quads[j + 0];
                    path.moveTo( p.x, p.y );
                    p = quads[j + 1];
                    path.lineTo( p.x, p.y );
                    p = quads[j + 2];
                    path.lineTo( p.x, p.y );
                    p = quads[j + 3];
                    path.lineTo( p.x, p.y );
                    path.close();
                    canvas.drawPath( path, lineBoundariesPaint );

                    // The skewed text (drawn by coordinate transform)
                    canvas.save();
                    Point p0 = quads[j + 0];
                    Point p1 = quads[j + 1];
                    Point p3 = quads[j + 3];

                    int dx1 = p1.x - p0.x;
                    int dy1 = p1.y - p0.y;
                    int dx2 = p3.x - p0.x;
                    int dy2 = p3.y - p0.y;

                    int sqrLength1 = dx1 * dx1 + dy1 * dy1;
                    int sqrLength2 = dx2 * dx2 + dy2 * dy2;

                    double angle = 180 * Math.atan2( dy2, dx2 ) / Math.PI;
                    double xskew = ( dx1 * dx2 + dy1 * dy2 ) / Math.sqrt( sqrLength2 );
                    double yskew = Math.sqrt( sqrLength1 - xskew * xskew );

                    textPaint.setTextSize( (float) yskew );
                    String line = lines[i];
                    Rect textBounds = new Rect();
                    textPaint.getTextBounds( lines[i], 0, line.length(), textBounds );
                    double xscale = Math.sqrt( sqrLength2 ) / textBounds.width();

                    canvas.translate( p0.x, p0.y );
                    canvas.rotate( (float) angle );
                    canvas.skew( -(float) ( xskew / yskew ), 0.0f );
                    canvas.scale( (float) xscale, 1.0f );

                    canvas.drawText( lines[i], 0, 0, textPaint );
                    canvas.restore();
                }
            }
            canvas.restore();

            // Drawing the 'progress'
            if( stability > 0 ) {
                int r = width / 50;
                int y = height - 175 - 2 * r;
                for( int i = 0; i < stability; i++ ) {
                    int x = width / 2 + 3 * r * ( i - 2 );
                    canvas.drawCircle( x, y, r, textPaint );
                }
            }
        }
    }


}