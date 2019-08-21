package info.androidhive.androidcamera.interfaces;

import okhttp3.Response;

public interface PostCallResponseHandler {
    void processResponse(Response response);
}
