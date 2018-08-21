package com.gruposantoro.sedeso.tools;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gruposantoro.sedeso.db.DataSource;
import com.gruposantoro.sedeso.service.ServicesUbicacion;
import com.gruposantoro.sedeso.service.ServicioSend;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by gruposantoro3 on 03/01/2018.
 */

public class ConexionService {

    private static int CONNECTION_TIME_OUT_EXCEPTION = 0;
    private static int SOKET_TIME_OUT_EXCEPTION = 1;
    private static int NULL_POINTER_EXCEPTION = 2;
    private static int MALFORMED_URL_EXCEPTION = 3;
    private static int IOE_EXCEPTION = 4;
    private static int ERROR_CODE = 5;
    private static String TIME_OUT = "TimeOut";
    private static String SERVER_ERROR = "SERVER_ERROR";
    private static String CONECTION_ERROR = "Error de conexión";
    private static String RESPONSE_TRUE = "TRUE";
    private static int TIME_OUT_IN_SECONDS = 300000;
    private Context context;
    private DataSource dataSource;
    private static final String IP = "http://209.58.140.44:8080/Sedeso";//209.58.140.44//144.217.254.92//192.168.100.117:8084

    public ConexionService(Context context) {
        this.context = context;
    }

    //FUNCTION 01
    public ArrayList<Object> login(String pass) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        try {
            JSONObject jason = new JSONObject();
            jason.put("telefono", "1");
            jason.put("password", pass);
            jason.put("option", "login");
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletCiudadano");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    resultado.add(true);
                    JSONObject objetoJSON = new JSONObject(res);
                    resultado.add(objetoJSON.getInt("id"));
                    resultado.add(objetoJSON.getInt("confiable"));
                    resultado.add(objetoJSON.getString("id_push"));
                    resultado.add(objetoJSON.getInt("activo"));
                } else {
                    resultado.add(false);
                    resultado.add("Contraseña incorrecta, favor de verificarla");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    public ArrayList<Object> incidencia(@Nullable int tipo, @Nullable String option, @Nullable int idCiudadano,
                                        @Nullable String lat, @Nullable String lang, @Nullable int partido,
                                        @Nullable String idEvento, String desc, @Nullable JSONArray img, @Nullable JSONObject data, @Nullable String cate, @Nullable String subC) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        JSONObject jason = new JSONObject();
        try {
            if (data == null) {
                jason.put("ciudadano", idCiudadano);
                jason.put("latitud", lat);
                jason.put("longitud", lang);
                jason.put("tipo", tipo);
                jason.put("habitacion", "");
                jason.put("partido", partido);
                jason.put("idEvento", idEvento);
                jason.put("option", option);
                jason.put("imagen", img);
                jason.put("descripcion", desc);
                jason.put("data",new JSONArray());
                jason.put("cate",cate);
                jason.put("subC",subC);
            } else {
                jason = data;
            }
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletAlerta");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    resultado.add(true);
                    resultado.add("Imagenes enviadas correctamente");
                } else {
                    if (data == null) {
                        dataSource = new DataSource(context);
                        dataSource.insertImg(jason.toString());
                        dataSource.closeDataBase();
                        Intent intent = new Intent(context, ServicioSend.class);
                        context.startService(intent);
                    }
                    resultado.add(false);
                    resultado.add("Las imagenes no pudieron enviarse en este momento, posteriormente se enviaran automaticamente");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    //FUNCTION 05
    public ArrayList<Object> sendForm(@Nullable String nombre, @Nullable String mts,
                                      @Nullable String lat, @Nullable String lang, @Nullable String domicilio,
                                      @Nullable String observa, @Nullable JSONArray foto, @Nullable JSONObject data) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        JSONObject jason = new JSONObject();
        try {
            if (data == null) {
                jason.put("nombre", nombre);
                jason.put("latitud", lat);
                jason.put("longitud", lang);
                jason.put("metros", mts);
                jason.put("observa", observa);
                jason.put("option", "insert");
                jason.put("fecha", df.format(gregorianCalendar.getTime()).replace("/", "-"));
                jason.put("domicilio", domicilio);
                jason.put("foto", foto);
            }else{
                jason = data;
            }
            System.out.println(jason);
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletAlerta");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    Log.e("RES",res);
                    JSONObject objetoJSON = new JSONObject(res);
                    if (objetoJSON.getString("Mensaje").equals("Almacenado Exitoxamente")){
                        resultado.add(true);
                        resultado.add(objetoJSON.getString("Mensaje"));
                    }else{
                        if (data == null) {
                            dataSource = new DataSource(context);
                            dataSource.insertImg(jason.toString());
                            dataSource.closeDataBase();
                            Intent intent = new Intent(context, ServicioSend.class);
                            context.startService(intent);
                        }
                        resultado.add(false);
                        resultado.add("Ocurrio un error al enviar la información, sera enviada automaticamente posteriormente");
                    }
                } else {
                    resultado.add(false);
                    resultado.add("Ocurrio un error al enviar la información");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
                if (data == null) {
                    dataSource = new DataSource(context);
                    dataSource.insertImg(jason.toString());
                    dataSource.closeDataBase();
                    Intent intent = new Intent(context, ServicioSend.class);
                    context.startService(intent);
                }
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    //FUNCTION 05
    public ArrayList<Object> sendActivacion(@Nullable String option, @Nullable int idCiudadano,
                                            @Nullable String lat, @Nullable String lang, @Nullable int partido,
                                            @Nullable String idEvento, @Nullable String desc, @Nullable JSONArray img, int idSubCatego, JSONArray dataForm, @Nullable String cate, @Nullable String subC, @Nullable JSONObject data) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        JSONObject jason = new JSONObject();
        try {
            if (data == null) {
                jason.put("ciudadano", idCiudadano);
                jason.put("latitud", lat);
                jason.put("longitud", lang);
                jason.put("tipo", idSubCatego);
                jason.put("habitacion", "");
                jason.put("partido", partido);
                jason.put("idEvento", idEvento);
                jason.put("option", option);
                jason.put("imagen", img);
                jason.put("data", dataForm);
                jason.put("descripcion", desc);
                jason.put("cate", cate);
                jason.put("subC", subC);
            }else{
                jason = data;
            }
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletActivacion");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    JSONObject objetoJSON = new JSONObject(res);
                    if (!objetoJSON.getString("lugar").equals("Alerta No enviada, Ocurrió un problema")){
                        resultado.add(true);
                        resultado.add(objetoJSON.getString("lugar"));
                    }else{
                        if (data == null) {
                            dataSource = new DataSource(context);
                            dataSource.insertImg(jason.toString());
                            dataSource.closeDataBase();
                            Intent intent = new Intent(context, ServicioSend.class);
                            context.startService(intent);
                        }
                        resultado.add(false);
                        resultado.add("Ocurrio un error al enviar la información, sera enviada automaticamente posteriormente");
                    }
                } else {
                    resultado.add(false);
                    resultado.add("Ocurrio un error al enviar la información");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
                if (data == null) {
                    dataSource = new DataSource(context);
                    dataSource.insertImg(jason.toString());
                    dataSource.closeDataBase();
                    Intent intent = new Intent(context, ServicioSend.class);
                    context.startService(intent);
                }
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
            if (data == null) {
                dataSource = new DataSource(context);
                dataSource.insertImg(jason.toString());
                dataSource.closeDataBase();
                Intent intent = new Intent(context, ServicioSend.class);
                context.startService(intent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    //FUNCTION 03-br
    public ArrayList<Object> preRegistro(String sede, String idEvento, int idPartido, @Nullable String desc, int idCiudadano) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            JSONObject jason = new JSONObject();
            jason.put("ciudadano", idCiudadano);
            jason.put("desc", desc);
            jason.put("partido", idPartido);
            jason.put("idEvento", idEvento);
            jason.put("latitud", ServicesUbicacion.lat);
            jason.put("longitud", ServicesUbicacion.lang);
            jason.put("sede", sede+" "+df.format(gregorianCalendar.getTime()).replace("/", "-").split( " ")[0]);
            jason.put("fecha", df.format(gregorianCalendar.getTime()).replace("/", "-"));
            jason.put("option", "addEventos");
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletActivacion");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    resultado.add(true);
                    JSONObject objetoJSON = new JSONObject(res);
                    resultado.add(objetoJSON.getString("idEvento"));
                } else {
                    resultado.add(false);
                    resultado.add("Error al crear la activación");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    //FUNCTION 04-br
    public ArrayList<Object> closeEvent(String cantP, String cantC, JSONArray typeC, String dura, String idEvento) throws JSONException {
        ArrayList<Object> resultado = new ArrayList<Object>();
        HttpURLConnection conn = null;
        JSONObject jason = new JSONObject();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            try {
                jason.put("cantP", Integer.parseInt(cantP));
                jason.put("cantCandi", Integer.parseInt(cantC));
                jason.put("duracion", Integer.parseInt(dura));
                jason.put("idEvento", idEvento);
                jason.put("fecha", df.format(gregorianCalendar.getTime()).replace("/", "-"));
                jason.put("typeCandi", typeC);
                jason.put("option", "closeEvento");
            } catch (NumberFormatException e) {
                jason.put("cantP", 0);
                jason.put("cantCandi", 0);
                jason.put("duracion", 0);
            }
            Log.e("JSON:", jason.toString());
            URL url = new URL(IP + "/ServletActivacion");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT_IN_SECONDS);
            conn.setReadTimeout(TIME_OUT_IN_SECONDS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            Encryptation enc = new Encryptation();
            os.write(enc.encryptWithKey(jason.toString()).getBytes("UTF-8"));
            os.close();
            int respuesta = conn.getResponseCode();
            Log.e("CODE HTTP", "" + respuesta);
            if (respuesta == HttpURLConnection.HTTP_OK) {
                resultado.add(RESPONSE_TRUE);
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String res = inputStreamToString(in);
                if (!res.equals("null")) {
                    JSONObject objetoJSON = new JSONObject(res);
                    if ("El evento se almaceno exitosamente".equals(objetoJSON.getString("Error"))) {
                        resultado.add(true);
                    } else {
                        resultado.add(false);
                        resultado.add(objetoJSON.getString("Error"));
                    }
                } else {
                    resultado.add(false);
                    resultado.add("Ocurrio un error al cerrar el evento");
                }
                in.close();
            } else {
                resultado.add(SERVER_ERROR);
                resultado.add(respuesta);
            }
            conn.disconnect();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(CONNECTION_TIME_OUT_EXCEPTION);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(SOKET_TIME_OUT_EXCEPTION);
        } catch (NullPointerException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(NULL_POINTER_EXCEPTION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resultado.add(TIME_OUT);
            resultado.add(MALFORMED_URL_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            resultado.add(CONECTION_ERROR);
            resultado.add(IOE_EXCEPTION);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultado;
    }

    private static String inputStreamToString(InputStream is) {
        String line = "";
        String repuesta = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
            rd.close();
        } catch (IOException ex) {
            Log.w("Aviso", ex.toString());
        }
        repuesta = total.toString();
        Log.e("Respuesta", repuesta);
        return repuesta;
    }

}
