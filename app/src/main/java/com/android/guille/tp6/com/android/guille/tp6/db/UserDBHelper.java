package com.android.guille.tp6.com.android.guille.tp6.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.guille.tp6.adapter.UserAdapter;
import com.android.guille.tp6.service.APIClientService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Guille on 14/05/2016.
 */
public class UserDBHelper extends SQLiteOpenHelper {

    APIClientService mService;
    private static UserDBHelper self = null;

    private UserDBHelper(Context context) {
        super(context, UserContract.DBNAME, null, UserContract.VERSION);
    }

    public static UserDBHelper getInstance(Context context){
        if(self == null)
            self = new UserDBHelper(context);

        return self;
    }

    public static UserDBHelper getInstance() throws Exception {
        if(self == null)
            throw new Exception("Context no seteado");

        return self;
    }

    public UserDBHelper setService(APIClientService service) {
        mService = service;
        return this;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserContract.UserTable.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public UserDBHelper setUsers(JSONArray users) {
        this.getWritableDatabase().execSQL("delete from "+UserContract.UserTable.TABLE_NAME);
        for(int i=0; i<users.length(); i++) {
            try {
                doCreate(users.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        UserAdapter.getInstance().notifyDataSetChanged();
        return this;
    }

    public int count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from " +
                        UserContract.UserTable.TABLE_NAME, null);
        return cursor.getCount();
    }

    protected void doCreate(JSONObject user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserContract.UserTable.MONGO_ID, user.optString("_id"));
        values.put(UserContract.UserTable.FIRSTNAME, user.optString("nombre"));
        values.put(UserContract.UserTable.LASTNAME, user.optString("apellido"));
        values.put(UserContract.UserTable.MAIL, user.optString("mail"));
        db.insert(UserContract.UserTable.TABLE_NAME, null, values);
    }

    public UserDBHelper create(JSONObject user) {
        doCreate(user);
        UserAdapter.getInstance().notifyDataSetChanged();
        if(mService != null) {
            mService.mBinder.addUser(user);
        }
        return this;
    }

    public UserDBHelper delete(JSONObject user) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = UserContract.UserTable._ID+" = ?";
        String[] args = {user.optString("_id")};
        db.delete(UserContract.UserTable.TABLE_NAME, selection, args);
        UserAdapter.getInstance().notifyDataSetChanged();
        if(mService != null) {
            try {
                user.put("_id", user.getString(UserContract.UserTable.MONGO_ID));
                user.remove("mongo_id");
                mService.mBinder.rmUser(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public UserDBHelper update(JSONObject user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserContract.UserTable.FIRSTNAME, user.optString("nombre"));
        values.put(UserContract.UserTable.LASTNAME, user.optString("apellido"));
        values.put(UserContract.UserTable.MAIL, user.optString("mail"));
        String selection = UserContract.UserTable._ID + " = ?";
        String[] args = {user.optString("_id")};
        db.update(UserContract.UserTable.TABLE_NAME, values, selection, args);
        UserAdapter.getInstance().notifyDataSetChanged();
        if(mService != null) {
            try {
                user.put("_id", user.getString(UserContract.UserTable.MONGO_ID));
                user.remove("mongo_id");
                mService.mBinder.setUser(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    protected JSONObject rowToJSON(Cursor cursor) {
        JSONObject user = new JSONObject();
        try {
            user.putOpt("_id", cursor.getLong(cursor.getColumnIndex(UserContract.UserTable._ID)));
            user.putOpt("nombre", cursor.getString(cursor.getColumnIndex(UserContract.UserTable.FIRSTNAME)));
            user.putOpt("apellido", cursor.getString(cursor.getColumnIndex(UserContract.UserTable.LASTNAME)));
            user.putOpt("mail", cursor.getString(cursor.getColumnIndex(UserContract.UserTable.MAIL)));
            user.putOpt("mongo_id", cursor.getString(cursor.getColumnIndex(UserContract.UserTable.MONGO_ID)));

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }

    public JSONObject read(Integer pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {String.valueOf(pos)};
        Cursor cursor = db.rawQuery(
                "select * from " +
                        UserContract.UserTable.TABLE_NAME +
                        " order by " +
                        UserContract.UserTable.LASTNAME + ", " +
                        UserContract.UserTable.FIRSTNAME +
                        " limit 1 offset ? "

                , args);
        if(cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        return rowToJSON(cursor);
    }

    public JSONObject get(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {String.valueOf(id)};
        Cursor cursor = db.rawQuery(
                "select * from " +
                        UserContract.UserTable.TABLE_NAME +
                        " where "+UserContract.UserTable.MONGO_ID+" = ?", args);
        if(cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        return rowToJSON(cursor);
    }
}
