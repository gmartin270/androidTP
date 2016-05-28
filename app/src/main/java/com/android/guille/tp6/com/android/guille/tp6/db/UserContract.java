package com.android.guille.tp6.com.android.guille.tp6.db;

import android.provider.BaseColumns;

/**
 * Created by Guille on 14/05/2016.
 */
public class UserContract {

    public static final String DBNAME = "userDB";
    public static final Integer VERSION = 1;

    public UserContract(){};

    public static abstract class UserTable implements BaseColumns{
        public static final String TABLE_NAME = "user";
        public static final String MONGO_ID = "mongo_id";
        public static final String FIRSTNAME = "nombre";
        public static final String LASTNAME = "apellido";
        public static final String MAIL = "mail";

        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                                                  _ID + " INTEGER PRIMARY KEY, " +
                                                  FIRSTNAME + " TEXT, " +
                                                  LASTNAME + " TEXT, " +
                                                  MAIL + " TEXT, " +
                                                  MONGO_ID + " TEXT );";
    }
}
