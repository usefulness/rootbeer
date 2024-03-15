package io.github.usefulness.rootbeer;


import androidx.annotation.NonNull;


public class RootBeerNative {

    public native boolean checkForRoot(@NonNull String[] pathArray);

    public native boolean checkForMagiskUDS();

    public native void setLogDebugMessages(boolean logDebugMessages);
}
