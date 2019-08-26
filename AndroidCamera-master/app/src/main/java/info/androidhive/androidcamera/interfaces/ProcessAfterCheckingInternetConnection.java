package info.androidhive.androidcamera.interfaces;

import info.androidhive.androidcamera.enums.ConnectionEnums;

public interface ProcessAfterCheckingInternetConnection {
    void processRequest(boolean connectionStatus, ConnectionEnums connectionEnums);
}
