package com.android.guille.tp6.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.adapter.UserAdapter;
import com.android.guille.tp6.com.android.guille.tp6.db.UserDBHelper;
import com.android.guille.tp6.entity.RestClient;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Guille on 15/04/2016.
 */
public class APIClientService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public APIBinder mBinder = new APIBinder();
    UserAdapter mAdapter = null;
    public String mURL; //= "http://192.168.1.18:8080/ws/"; //"http://tm5-agmoyano.rhcloud.com/";//"http://192.168.1.18:8080/ws/";
    private Timer timer = new Timer();
    private SharedPreferences sharedPref;
    int MAIN_ACTIVITY_REQUEST = 1;
    private boolean mBound;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private static String usersFile = "users.json";
    private UserDBHelper db;

    public APIClientService(){}

    public class APIBinder extends Binder{
        private static final int ADD = 1;
        private static final int DEL = 2;
        private static final int UPD = 3;
        private ArrayList<JSONObject> pending = new ArrayList<JSONObject>();
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
                RestClient.get(mURL + "user", new RestClient.Result() {
                    @Override
                    public void onResult(Object result) {
                        try {
                            JSONArray resArray = (JSONArray) result;

                            if (!mBound) {
                                int newCount=0;
                                JSONObject newUser = new JSONObject();
                                for(int i = 0; i < resArray.length(); i++) {
                                    JSONObject user = resArray.getJSONObject(i);
                                    boolean isNew = true;
                                    if(db.get(user.getString("_id")) == null) {
                                        isNew = false;
                                        break;
                                    }
                                    if(isNew) {
                                        newCount++;
                                        newUser = user;
                                    }
                                }

                                if(newCount > 0) {
                                    if(newCount==1) {
                                        mBuilder.setContentText(newUser.getString("nombre")+" "+
                                                newUser.getString("apellido"));
                                    } else {
                                        mBuilder.setContentText(newCount+" "+getString(R.string.newUser));
                                    }
                                    mNotificationManager.notify(1, mBuilder.build());
                                }
                            }

                            db.setUsers(resArray);
                        }catch (JSONException e){
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

            }catch(IOException e){
                Toast.makeText(APIClientService.this, e.getMessage(), Toast.LENGTH_SHORT);
            }
        }

        public void addUser(final JSONObject usuario) {
            final JSONObject pendingUser = usuario;
            try {
                RestClient.post(mURL + "user", usuario, new RestClient.Result() {
                    @Override
                    public void onResult(Object result) {
                        findUsers();
                    }

                    @Override
                    public void onError(String message) {
                        JSONObject payload = new JSONObject();
                        try {
                            payload.put("action", ADD);
                            payload.put("user", pendingUser);
                            pending.add(payload);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setUser(JSONObject usuario) {
            final JSONObject pendingUser = usuario;
            try {
                RestClient.put(mURL + "user/" + usuario.getString("_id"), usuario, new RestClient.Result() {
                    public void onResult(Object result) {
                        findUsers();
                    }
                    public void onError(String message) {
                        JSONObject payload = new JSONObject();
                        try {
                            payload.put("action", UPD);
                            payload.put("user", pendingUser);
                            pending.add(payload);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void rmUser(JSONObject usuario) {
            final JSONObject pendingUser = usuario;
            try {
                RestClient.delete(mURL + "user/" + usuario.getString("_id"), new RestClient.Result() {
                    @Override
                    public void onResult(Object result) {
                        findUsers();
                    }
                    @Override
                    public void onError(String message) {
                        JSONObject payload = new JSONObject();
                        try {
                            payload.put("action", DEL);
                            payload.put("user", pendingUser);
                            pending.add(payload);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        RestClient.setContext(this);
        db = UserDBHelper.getInstance(APIClientService.this).setService(APIClientService.this);
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(APIClientService.this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(APIClientService.this, MAIN_ACTIVITY_REQUEST, i, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle(getString(R.string.newUser)).setContentIntent(pi);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTimer();
        setURL();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBound = true;
        mNotificationManager.cancelAll();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBound = false;
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onDestroy() {
        timer.cancel();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("interval")) {
            setTimer();
        }
        if (key.equals("url")) {
            setURL();
        }
    }

    private void setURL(){
        mURL = sharedPref.getString("pref_URL_API", "http://tm5-agmoyano.rhcloud.com/");
    }

    private void setTimer(){
        Integer sec = Integer.valueOf(sharedPref.getString("interval","5"));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //mBinder.resolvePending();
                mBinder.findUsers();
            }
        }, 0, sec * 1000);
    }
}
