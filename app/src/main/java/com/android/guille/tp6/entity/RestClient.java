package com.android.guille.tp6.entity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Guille on 08/04/2016.
 */
public class RestClient {

    private static Context context;

    private static class Config{
        public String url = null;
        public String method = null;
        public Result handler = null;
        public JSONObject data = null;
    }

    public static class BGResult{
        public String error = null;
        public String result = null;
        public Result handler = null;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        RestClient.context = context;
    }

    public interface Result{
        public void onResult(Object result);
        public void onError(String message);
    }

    private static class RestTask extends AsyncTask<Config, Void, BGResult>{

        @Override
        protected BGResult doInBackground(Config... configs) {
            Config config = configs[0];
            OutputStreamWriter writer = null;
            BGResult result = new BGResult();
            result.handler = config.handler;

            try{
                URL url = new URL(config.url);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod(config.method);
                conn.setDoInput(true);

                if(config.data != null){
                    conn.setDoOutput(true);
                    conn.setChunkedStreamingMode(0);
                    conn.setRequestProperty("Content-Type", "application/json");

                    writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(config.data.toString());
                    writer.close();
                }

                result.result = IOUtils.toString(conn.getInputStream());
            }catch (IOException e) {
                result.error = e.getMessage();
            }finally {
                if(writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(BGResult result) {
            if(result.error != null)
                result.handler.onError(result.error);
            else{
                JSONTokener tokener = new JSONTokener(result.result);

                try{
                    result.handler.onResult(tokener.nextValue());
                }catch (JSONException e){
                    result.handler.onError(e.getMessage());
                }
            }
        }
    }

    public static void get(String url, Result handler) throws IOException{
        Config config = new Config();
        config.url = url;
        config.handler = handler;
        config.method = "GET";

        connection(config, handler);
    }

    public static void post(String url, JSONObject data, Result handler) throws IOException{
        Config config = new Config();
        config.url = url;
        config.handler = handler;
        config.method = "POST";
        config.data = data;

        connection(config, handler);
    }

    public static void put(String url, JSONObject data, Result handler) throws IOException{
        Config config = new Config();
        config.url = url;
        config.handler = handler;
        config.method = "PUT";
        config.data = data;

        connection(config, handler);
    }

    public static void delete(String url, Result handler) throws IOException{
        Config config = new Config();
        config.url = url;
        config.handler = handler;
        config.method = "DELETE";

        connection(config, handler);
    }

    public static void connection(Config config, Result handler) throws IOException{
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo != null)
            new RestTask().execute(config);
        else
            handler.onError("No hay conexión a Internet");
    }
}
