package com.android.guille.tp6.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.entity.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Guille on 02/04/2016.
 */
public class UserAdapter extends BaseAdapter {

    private Context mContext;
    private RestClient.Result resultHandler = null;
    JSONArray usrs = new JSONArray();
    private String id;

    private static UserAdapter instance;

    private UserAdapter(Context context){
        mContext = context;
    }

    public static UserAdapter getInstance(Context context){
        if(instance == null)
            instance = new UserAdapter(context);

        return instance;
    }

    public void setList(JSONArray personas){
        this.usrs = personas;
        notifyDataSetChanged();
    }

    public JSONArray getUsrs(){
        return this.usrs;
    }

    @Override
    public int getCount() {
        if(usrs != null)
            return usrs.length();
        else
            return 0;
    }

    @Override
    public JSONObject getItem(int position) {
        JSONObject result = null;

        try{
            result = usrs.getJSONObject(position);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            JSONObject persona = (JSONObject) getItem(position);
            id = persona.getString("_id");

            if (convertView == null) {
                LayoutInflater li = LayoutInflater.from(mContext);
                convertView = li.inflate(R.layout.item_persona, null);
            }

            try {
                TextView nombres = (TextView) convertView.findViewById(R.id.txtNomCompleto);
                nombres.setText(persona.getString("nombre") + " " + persona.getString("apellido"));

                TextView email = (TextView) convertView.findViewById(R.id.txtEmailV);
                email.setText(persona.getString("mail"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ImageView delete = (ImageView) convertView.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.deleteUserTitle)
                            .setMessage(R.string.deleteUserMessage)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MainActivity)mContext).rmUser(id);
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
        }catch (JSONException e){
            Toast.makeText(mContext,e.getMessage(), Toast.LENGTH_SHORT);
        }

        return convertView;
    }
}
