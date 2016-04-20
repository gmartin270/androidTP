package com.android.guille.tp6.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.adapter.UserAdapter;
import com.android.guille.tp6.entity.RestClient;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    int MAIN_ACTIVITY_REQUEST = 1;
    private boolean mBound;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private static String usersFile = "users.json";

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
            RestClient.setContext(activity);
        }

        public void findUsers(){
            try{
                if(mActivity != null) {
                    RestClient.get(mURL, new RestClient.Result() {
                        @Override
                        public void onResult(Object result) {
                            try {
                                JSONArray resArray = (JSONArray) result;

                                if (mBound) {
                                    mAdapter = UserAdapter.getInstance(mActivity);
                                    mAdapter.setList((JSONArray) result);
                                } else {
                                    int newCount = 0;
                                    JSONObject newUser = new JSONObject();
                                    FileInputStream fis = openFileInput(usersFile);
                                    JSONArray usersState = new JSONArray(IOUtils.toString(fis));

                                    for (int i = 0; i < resArray.length(); i++) {
                                        JSONObject user = resArray.getJSONObject(i);
                                        boolean isNew = true;

                                        for (int j = 0; j < usersState.length(); j++) {
                                            if (user.getString("_id").equals(usersState.getJSONObject(j).getString("_id"))) {
                                                isNew = false;
                                                break;
                                            }
                                        }

                                        if (isNew) {
                                            newCount++;
                                            newUser = user;
                                        }
                                    }

                                    if (newCount > 0) {
                                        if (newCount == 1) {
                                            mBuilder.setContentText(newUser.getString("nombre" + " " + newUser.getString("apellido")));
                                        } else {
                                            mBuilder.setContentText(newCount + " " + R.string.newUser);
                                        }

                                        mNotificationManager.notify(1, mBuilder.build());
                                    }
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }catch (FileNotFoundException e){
                                e.printStackTrace();
                            }catch (IOException e) {
                                e.printStackTrace();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
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
        RestClient.setContext(this);
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(APIClientService.this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(APIClientService.this, MAIN_ACTIVITY_REQUEST, i, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle(getString(R.string.newUser)).setContentIntent(pi);
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                mBinder.findUsers();
            }
        }, 0, 6000);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBound = true;
        mNotificationManager.cancelAll();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        try {
            FileOutputStream fos = openFileOutput(usersFile, MODE_PRIVATE);
            mAdapter = UserAdapter.getInstance(null);

            fos.write(mAdapter.getUsrs().toString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBound = false;
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
