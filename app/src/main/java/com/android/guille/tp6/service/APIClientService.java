package com.android.guille.tp6.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.adapter.UserAdapter;
import com.android.guille.tp6.entity.RestClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Guille on 15/04/2016.
 */
public class APIClientService extends Service {
    APIBinder mBinder = new APIBinder();
    UserAdapter mAdapter = null;
    public static String mURL = "http://tm5-agmoyano.rhcloud.com/";//"http://192.168.1.18:8080/ws/";
    private Timer timer = new Timer();


    public APIClientService(){}

    public class APIBinder extends Binder{
        private MainActivity mActivity = null;
        private Context context;

        RestClient.Result resultHandler = new RestClient.Result() {
            @Override
            public void onResult(Object result) {
                findUsers();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(APIClientService.this, message, Toast.LENGTH_LONG);
            }
        };

        public void setActivity(MainActivity activity) {
            mActivity = activity;
            RestClient.context = activity;
        }

        public void findUsers(){
            try{
                if(mActivity != null) {
                    RestClient.get(mURL, new RestClient.Result() {
                        @Override
                        public void onResult(Object result) {
                            mAdapter = UserAdapter.getInstance(mActivity);
                            mAdapter.setList((JSONArray) result);
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(APIClientService.this, message, Toast.LENGTH_SHORT);
                        }
                    });
                }
            }catch(IOException e){
                Toast.makeText(APIClientService.this, e.getMessage(), Toast.LENGTH_SHORT);
            }
        }

        public void addUser(JSONObject usuario) {

            try {
                RestClient.post(mURL, usuario, resultHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setUser(String id, JSONObject usuario) {
            try {
                RestClient.put(mURL + id, usuario, resultHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void rmUser(String id) {
            try {
                RestClient.delete(mURL + id, resultHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                mBinder.findUsers();
            }
        }, 0, 5000);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
