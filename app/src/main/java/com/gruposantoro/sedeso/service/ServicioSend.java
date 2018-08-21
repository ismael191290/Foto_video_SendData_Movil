package com.gruposantoro.sedeso.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gruposantoro.sedeso.R;
import com.gruposantoro.sedeso.db.DataSource;
import com.gruposantoro.sedeso.tools.ConexionService;
import com.gruposantoro.sedeso.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gruposantoro3 on 04/01/2018.
 */

public class ServicioSend extends Service {

    Timer timer;
    private Context context;
    private ArrayList<Integer> ids;
    private ArrayList<String> jsons;

    @Override
    public void onCreate() {
        super.onCreate();
        send();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void send(){
        jsons = new ArrayList<>();
        ids = new ArrayList<>();
        context = getApplicationContext();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Tools.checkNow(context)) {
                    if (timer != null) {
                        timer.cancel();
                    }
                    DataSource data = new DataSource(context);
                    Cursor c = data.data();
                    Log.e("SIZE", c.getCount() + "");
                    while(c.moveToNext()){
                        ids.add( c.getInt(0));
                        jsons.add(c.getString(1));
                    }
                    Log.e("FIN",  "TERMINA");
                    c.close();
                    int size = jsons.size();
                    for(int i =0; i<size;i++) {
                        JSONObject json = null;
                        try {
                            json = new JSONObject(jsons.get(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayList<Object> respuesta = new ArrayList<>();
                        try {
                            respuesta = new ConexionService(context).sendForm(null,null,null,null,null,null,null,json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (respuesta.size() > 0) {
                            String res = respuesta.get(0).toString();
                            if (res.equals(context.getString(R.string.response_true))) {
                                if ((boolean) respuesta.get(1)) {
                                    data.deleteImg(ids.get(i));
                                }
                            }
                        }
                    }
                    Cursor c2 = data.data();
                    if (c2.getCount() <= 0) {
                        c2.close();
                        data.closeDataBase();
                        stopSelf();
                    }else{
                        c2.close();
                        data.closeDataBase();
                        send();
                    }
                } else {
                    Log.e("ELSE_1", "OK");
                }

            }
           }, 1000, 5000);
        //}, 300000, 300000);
    }
}
