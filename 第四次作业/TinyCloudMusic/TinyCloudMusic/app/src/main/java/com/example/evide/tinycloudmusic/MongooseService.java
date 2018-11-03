/**
 * This service handles mongoose HTTP service
 * In this project, mongoose HTTP server was used for transmission of files and commands
 * Mongoose Embedded Web Server Library - Mongoose is more than an embedded webserver. It is a
 * multi-protocol embedded networking library with functions including TCP, HTTP client and server,
 * WebSocket client and server, MQTT client and broker and much more. https://www.cesanta.com
 */

package com.example.evide.tinycloudmusic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MongooseService extends Service {

    private MongooseBinder binder = new MongooseBinder();

    static {
        //This action will load mongoose HTTP server library(mongoose.so)
        System.loadLibrary("native-lib");
    }

    public MongooseService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Notice", "MongooseService Started");
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean status = SimpleMongooseHTTPServerTest(4567);
                Log.e("Error", "Mongoose Status: " + status);
            }
        }.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    public class MongooseBinder extends Binder {
        public int getCmdFlag() {
            return GetCmdFlag();
        }

        public void clrCmdFlag() {
            SetCmdFlag(0);
        }

        public String getCmdArg() {
            return GetCmdArg();
        }
    }

    public native boolean SimpleMongooseHTTPServerTest(int port);
    public native int GetCmdFlag();
    public native void SetCmdFlag(int f);
    public native String GetCmdArg();
}
