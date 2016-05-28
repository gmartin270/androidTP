package com.android.guille.tp6.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.guille.tp6.R;
import com.android.guille.tp6.adapter.UserAdapter;
import com.android.guille.tp6.com.android.guille.tp6.db.UserDBHelper;
import com.android.guille.tp6.fragment.FormFragment;
import com.android.guille.tp6.fragment.ListFragment;
import com.android.guille.tp6.fragment.SettingsFragment;
import com.android.guille.tp6.service.APIClientService;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    FragmentManager manager;
    ListFragment list = new ListFragment();
    FormFragment form = new FormFragment();
    private Boolean isPort = null;
    private APIClientService.APIBinder mBinder;
    UserDBHelper mDB;

    public Boolean getIsPort() {
        return isPort;
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (APIClientService.APIBinder)service;
            mBinder.setActivity(MainActivity.this);
            mBinder.findUsers();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDB = UserDBHelper.getInstance(this);

        UserAdapter userAdapter = UserAdapter.getInstance();
        userAdapter.setmContext(MainActivity.this);

        Intent i = new Intent(this, APIClientService.class);
        startService(i);
        bindService(i, mConnection, BIND_AUTO_CREATE);

        View container = findViewById(R.id.container);
        isPort = container!=null;
        manager = getSupportFragmentManager();

        if(isPort) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, list);
            transaction.commit();
        } else {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.listContainer, list);
            transaction.replace(R.id.formContainer, form);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                SettingsFragment settings = new SettingsFragment();
                FragmentTransaction transaction = manager.beginTransaction();

                if(isPort){
                    transaction.replace(R.id.container, settings);

                }else{
                    transaction.replace(R.id.listContainer, settings);
                    transaction.replace(R.id.formContainer, settings);
                }
                transaction.addToBackStack("settings");
                transaction.commit();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public void selectUser(Object persona)throws Exception{
        if(isPort) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, form);
            transaction.commit();
        }

        if(persona!=null)
            form.selectUser((JSONObject) persona);
    }

    public void setUser(JSONObject user, String Id) {
        if(user != null) {
            if (Id != null && Id.length() > 0)
                mDB.update(user);
            else
                mDB.create(user);
        }

        if (isPort) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, list);
            transaction.commit();
        }
    }

    public void rmUser(JSONObject user){
        mDB.delete(user);
    }
}
