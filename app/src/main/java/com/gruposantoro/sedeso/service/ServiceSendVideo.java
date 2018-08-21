package com.gruposantoro.sedeso.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.gruposantoro.sedeso.PrincipalActivity;
import com.gruposantoro.sedeso.R;
import com.gruposantoro.sedeso.db.DataSource;
import com.gruposantoro.sedeso.services.Video;
import com.gruposantoro.sedeso.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gruposantoro3 on 19/01/2018.
 */

public class ServiceSendVideo extends Service {


    Timer timer;
    private Context context;
    private static boolean TERMINA_SERVICIO = false;
    private DataSource data;

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

        context = getApplicationContext();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Tools.checkNow(context)) {
                    data = new DataSource(context);
                    final Cursor c = data.video();
                    if (c.moveToNext()) {
                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        RequestBody domiPart = RequestBody.create(MultipartBody.FORM,c.getString(3));
                        RequestBody namePart = RequestBody.create(MultipartBody.FORM,c.getString(1));
                        RequestBody latPart = RequestBody.create(MultipartBody.FORM,c.getString(4));
                        RequestBody langPart = RequestBody.create(MultipartBody.FORM,c.getString(5));
                        RequestBody mtsPart = RequestBody.create(MultipartBody.FORM,c.getString(2));
                        RequestBody observaPart = RequestBody.create(MultipartBody.FORM,c.getString(6));
                        RequestBody fechaPart = RequestBody.create(MultipartBody.FORM,df.format(gregorianCalendar.getTime()).replace("/", "-"));
                        File originFile = new File(c.getString(7));
                        Log.e("FILE","------"+originFile.getName());
                        Uri u = Uri.parse(c.getString(7));
                        Log.e("URI","------"+u.getPath());
                        RequestBody videoPart = RequestBody.create(MediaType.parse(getMimeType(c.getString(7))), originFile);
                        OkHttpClient.Builder build = new OkHttpClient.Builder();
                        build.connectTimeout(120, TimeUnit.SECONDS);
                        build.readTimeout(120,TimeUnit.SECONDS);
                        OkHttpClient client = build.build();
                        MultipartBody.Part file = MultipartBody.Part.createFormData("video",originFile.getName(),videoPart);
                        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("http://209.58.140.44:8080/Sedeso/").addConverterFactory(GsonConverterFactory.create());
                        builder.client(client);
                        Retrofit retrofit = builder.build();
                        Video cliente = retrofit.create(Video.class);
                        Call<ResponseBody> call = cliente.uploadVideo(namePart,domiPart,latPart,langPart,mtsPart,observaPart,fechaPart,file);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                                if (response.code() == HttpURLConnection.HTTP_OK) {
                                    try {
                                        JSONObject json = new JSONObject(response.body().string());
                                        Log.e("RESPONSE",json.toString());
                                        if (json.optString("Mensaje").equals("Almacenado Exitoxamente")) {
                                            DataSource data = new DataSource(ServiceSendVideo.this);
                                            data.deleteVideo();
                                            data.closeDataBase();
                                            stopSelf();

                                        } else {
                                            send();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                } else {
                                    send();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("FAIL","-------"+call.toString());
                                t.printStackTrace();
                                send();
                            }
                        });
                        c.close();
                        data.closeDataBase();
                    }else{
                        c.close();
                        data.closeDataBase();
                        stopSelf();
                    }
                    timer.cancel();
                } else {
                    Log.e("ELSE_1", "OK");
                }
            }
        }, 3000, 3000);
        //}, 300000, 300000);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
