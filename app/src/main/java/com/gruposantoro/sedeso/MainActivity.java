package com.gruposantoro.sedeso;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;

import com.gruposantoro.sedeso.db.DataSource;
import com.gruposantoro.sedeso.service.ServicesUbicacion;
import com.gruposantoro.sedeso.service.ServicioSend;
import com.gruposantoro.sedeso.services.Video;
import com.gruposantoro.sedeso.tools.ConexionService;
import com.gruposantoro.sedeso.tools.PermissionUtil;
import com.gruposantoro.sedeso.tools.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class MainActivity extends AppCompatActivity {


    private final static int TAKE_PICTURE = 599;
    private final static int TAKE_VIDEO = 799;
    private String imagePath = "";
    private Uri uriImg;
    private ArrayList<String> arrayImg;
    private String realPath;
    private DataSource dataSource;
    private boolean internet;
    private static final int REQUEST_CALENDAR = 0;
    private ProgressDialog pDialog;
    private Button btnVideo,btnImg,btnEnviar,btnNew;
    private boolean gps = false;
    private EditText txtName,txtDomicilio,txtMts,txtObserva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnImg = findViewById(R.id.btnFoto);
        btnVideo = findViewById(R.id.btnVideo);

        btnEnviar = findViewById(R.id.btnEnviar);
        txtName = findViewById(R.id.txtName);
        txtDomicilio = findViewById(R.id.txtDomi);
        txtMts = findViewById(R.id.txtMts);
        txtObserva = findViewById(R.id.txtObserva);
        arrayImg = new ArrayList<>();
        verificar();
        Intent intent = new Intent(MainActivity.this, ServicesUbicacion.class);
        intent.putExtra("BanderaPosicion",false);
        startService(intent);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.checkStrickMode();
                realPath = Tools.video2(MainActivity.this, TAKE_VIDEO);
            }
        });
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayImg = new ArrayList<>();
                    uriImg = Tools.photo(MainActivity.this, TAKE_PICTURE);
            }
        });
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("INFO","PATH:"+imagePath+" video");
                if (arrayImg.size()==0 && realPath!=null){
                    uploadVideoRetrofit();
                }else if (arrayImg.size()!=0 && realPath==null){
                    send();
                }else{
                    Tools.imprime("Es necesario tomar foto o video para enviar la información",MainActivity.this);
                }
            }
        });
        IntentFilter filterGPS = new IntentFilter("gps_disable");
        filterGPS.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(onGPS, filterGPS);
        //pDialog = Tools.esperaDialog(MainActivity.this,"Inicializando aplicación...");
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e("TIMER","Entro");
                //Tools.cancelarDialog(pDialog);
                if (!gps){
                    t.cancel();
                    Tools.notiLocal("gps_disable","showGPS",true,MainActivity.this);
                }else{
                    t.cancel();
                }
            }
        },3000,1000);
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                internet = Tools.isOnlineNet();
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = Tools.esperaDialog(MainActivity.this, "Comprobando internet");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Tools.cancelarDialog(pDialog);
            }
        }.execute();
    }

    private void send() {
        if (Tools.checkNow(MainActivity.this) && internet){
            pDialog = Tools.esperaDialog(MainActivity.this,"Enviando información");
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {

                    try {
                        JSONArray array = new JSONArray();
                        Tools t = new Tools();
                        for (String s: arrayImg){
                            array.put(t.getImg64(MainActivity.this,s));
                        }
                        t=null;
                        return new ConexionService(MainActivity.this).sendForm(txtName.getText().toString(),
                                txtMts.getText().toString(),ServicesUbicacion.lat,ServicesUbicacion.lang,txtDomicilio.getText().toString(),txtObserva.getText().toString(),array, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return new ArrayList<>();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    Tools.cancelarDialog(pDialog);
                    ArrayList respuesta = (ArrayList) o;
                    if (respuesta.size() > 0) {
                        String res = respuesta.get(0).toString();
                        if (res.equals(getString(R.string.response_true))) {
                            if ((boolean) respuesta.get(1)) {
                                Tools.imprime("Información enviada", MainActivity.this);
                                btnImg.setEnabled(true);
                                btnImg.setBackground(getDrawable(R.drawable.btn_oval));
                                btnVideo.setEnabled(true);
                                btnVideo.setBackground(getDrawable(R.drawable.btn_oval));
                                uriImg=null;
                                imagePath=null;
                                realPath=null;
                                arrayImg=new ArrayList<>();
                            } else {
                                Tools.imprime((String) respuesta.get(2),MainActivity.this);
                            }
                        } else if (res.equals(getString(R.string.response_server_error))) {
                            Tools.imprimeFinish(getString(R.string.str_error_server) + "\n (05:07:" + respuesta.get(1).toString() + "), la información sera enviada posteriormente", MainActivity.this, MainActivity.this);
                        } else if (res.equals(getString(R.string.response_time_out))) {
                            //aqui ira si no hay internet
                            Tools.imprimeFinish(getString(R.string.str_instente_mas_tarde) + "\n (05:07:" + respuesta.get(1) + "), la información sera enviada posteriormente", MainActivity.this, MainActivity.this);
                        } else {
                            Tools.imprime(getString(R.string.str_list_error) + "\n (05:07:01)", MainActivity.this);
                        }
                    }
                }
            }.execute();
        } else{
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    dataSource = new DataSource(MainActivity.this);
                    dataSource.closeDataBase();
                    GregorianCalendar gregorianCalendar = new GregorianCalendar();
                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    try {
                        JSONArray array = new JSONArray();
                        Tools t = new Tools();
                        for (String s: arrayImg){
                            array.put(t.getImg64(MainActivity.this,s));
                        }
                        t=null;
                        JSONObject jason = new JSONObject();
                        jason.put("nombre", txtName.getText().toString());
                        jason.put("latitud", ServicesUbicacion.lat);
                        jason.put("longitud", ServicesUbicacion.lang);
                        jason.put("metros", txtName.getText().toString());
                        jason.put("observa", txtName.getText().toString());
                        jason.put("foto", array);
                        jason.put("fecha", df.format(gregorianCalendar.getTime()).replace("/", "-"));
                        jason.put("domicilio", txtName.getText().toString());
                        dataSource = new DataSource(MainActivity.this);
                        dataSource.insertImg(jason.toString());
                        dataSource.closeDataBase();
                        Intent intent = new Intent(MainActivity.this, ServicioSend.class);
                        MainActivity.this.startService(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // pDialog = Tools.esperaDialog(MainActivity.this, "Enviando Datos");
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    Tools.cancelarDialog(pDialog);
                    Tools.imprimeFinish("No cuenta con conexión a internet, las imagenes seran enviadas automaticamente más tarde", MainActivity.this, MainActivity.this);
                }
            }.execute();
        }
    }

    private void uploadVideoRetrofit(){
        String [] path = new String[1];
        path[0] = getRealPathFromFile();
        pDialog = Tools.esperaDialog(MainActivity.this,"Cargando video");
        if (ServicesUbicacion.lat.equals("") || ServicesUbicacion.lang.equals("")){
            Tools.imprime("Las cordenadas no pudieron ser obtenidas espere un momento",MainActivity.this);
            return;
        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Log.e("PATH","------"+path[0]);
        RequestBody domiPart = RequestBody.create(MultipartBody.FORM,txtDomicilio.getText().toString());
        RequestBody namePart = RequestBody.create(MultipartBody.FORM,txtName.getText().toString());
        RequestBody latPart = RequestBody.create(MultipartBody.FORM,ServicesUbicacion.lat);
        RequestBody langPart = RequestBody.create(MultipartBody.FORM,ServicesUbicacion.lang);
        RequestBody mtsPart = RequestBody.create(MultipartBody.FORM,txtMts.getText().toString());
        RequestBody observaPart = RequestBody.create(MultipartBody.FORM,txtObserva.getText().toString());
        RequestBody fechaPart = RequestBody.create(MultipartBody.FORM,df.format(gregorianCalendar.getTime()).replace("/", "-"));
        File originFile = new File(path[0]);
        Log.e("FILE","------"+originFile.getName());
        Uri u = Uri.parse(path[0]);
        Log.e("URI","------"+u.getPath());
        RequestBody videoPart = RequestBody.create(MediaType.parse(getMimeType(path[0])), originFile);
        OkHttpClient.Builder build = new OkHttpClient.Builder();
        build.connectTimeout(300, TimeUnit.SECONDS);
        build.readTimeout(300,TimeUnit.SECONDS);
        OkHttpClient client = build.build();
        MultipartBody.Part file = MultipartBody.Part.createFormData("video",originFile.getName(),videoPart);
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl("http://144.217.254.92:8080/Sedeso/").addConverterFactory(GsonConverterFactory.create());
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
                            Tools.imprime("Video enviado exitosamente", MainActivity.this);
                            btnImg.setEnabled(true);
                            btnImg.setBackground(getDrawable(R.drawable.btn_oval));
                            btnVideo.setEnabled(true);
                            btnVideo.setBackground(getDrawable(R.drawable.btn_oval));
                            uriImg=null;
                            imagePath=null;
                            realPath=null;
                        } else {
                            Tools.imprime("El video no pudo ser enviado, favor de intentarlo nuevamente ", MainActivity.this);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                } else {
                    Tools.imprime("Ocurrio un error en el servidor, favor de intentarlo nuevamente", MainActivity.this);
                }
                Tools.cancelarDialog(pDialog);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("FAIL","-------"+call.toString());
                t.printStackTrace();
                Tools.cancelarDialog(pDialog);
            }
        });
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getRealPathFromFile() {
        String stringPath = "";
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        stringPath = path.getAbsolutePath() + "/sedeso/" + realPath;
        return stringPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_VIDEO && resultCode == RESULT_OK) {
            if (realPath != null) {
                btnImg.setEnabled(false);
                btnImg.setBackground(getDrawable(R.drawable.btn_disable));
            } else {
                Tools.imprime("Ocurrio un error al tomar el video, favor de tomarlo nuevamente", MainActivity.this);
            }
        } else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            if (uriImg != null) {
                btnVideo.setEnabled(false);
                btnVideo.setBackground(getDrawable(R.drawable.btn_disable));
                arrayImg.add(getRealPathFromURI(uriImg));
                if (arrayImg.size()<2){
                    uriImg = Tools.photo(MainActivity.this, TAKE_PICTURE);
                }
            } else {
                uriImg = Tools.photo(MainActivity.this, TAKE_PICTURE);
            }
        }else{
            if (!gps){
                Tools.encenderGPS(MainActivity.this);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        return path;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CALENDAR);


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CALENDAR);

        }
    }

    private void verificar(){
        if ( ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermission();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CALENDAR) {
            Log.e("TAG", "Received response for contact permissions request.");
            if (PermissionUtil.verifyPermissions(grantResults)) {
                verificar();
                Intent intent = new Intent(MainActivity.this, ServicesUbicacion.class);
                intent.putExtra("BanderaPosicion",false);
                startService(intent);

            } else {
                Tools.imprimeFinish("Se denegaron los permisos para el acceso a la lectura de imagenes," +
                        " los cuales son " +
                        "necesarios para el uso de esta aplicación",MainActivity.this,MainActivity.this);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private BroadcastReceiver onGPS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("datosGPS")) {
                try {
                    gps = intent.getBooleanExtra("datosGPS", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(intent.hasExtra("showGPS")) {
                Tools.encenderGPS(MainActivity.this);
            } else {
                Log.e("ELSE1", "Llega");
            }
        }
    };

}
