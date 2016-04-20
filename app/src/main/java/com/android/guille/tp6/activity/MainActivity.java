package com.android.guille.tp6.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.guille.tp6.R;
import com.android.guille.tp6.entity.Persona;
import com.android.guille.tp6.fragment.FormFragment;
import com.android.guille.tp6.fragment.ListFragment;
import com.android.guille.tp6.service.APIClientService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    FragmentManager manager;
    ListFragment list = new ListFragment();
    FormFragment form = new FormFragment();
    private Boolean isPort = null;
    private Persona usr;
    private Integer posicion;
    private APIClientService.APIBinder mBinder;

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

        Intent i = new Intent(this, APIClientService.class);
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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public void selectUser(Object persona, Integer posicion)throws Exception{
        if(isPort) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, form);
            transaction.commit();
        }

        if(persona!=null)
            form.selectUser((Persona)persona, posicion);
    }

    public void setUser(Persona user, String id) {
        try {
            if (user != null) {
                JSONObject jPersona = new JSONObject();
                jPersona.put("nombre", user.getNombre());
                jPersona.put("apellido", user.getApellido());
                jPersona.put("mail", user.getEmail());

                if (id == null) {
                    mBinder.addUser(jPersona);
                } else {
                    jPersona.put("_id", user.getId());
                    mBinder.setUser(id, jPersona);
                }
            }

            if (isPort) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container, list);
                transaction.commit();
            }
        }catch (JSONException e){
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    public void rmUser(String id){
        if(mBinder != null){
            mBinder.rmUser(id);
        }
    }
}
