package com.example.esp32_cam_game_for_app;



import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class MainActivity extends AppCompatActivity{


    // AP ESP-PTZ-1
    String url = "http://192.168.1.160";


    Button top, bottom, left, right, attack;
    ImageView camera_1;
    TextView live_text;




    private final int CAMERA = 1;
    private final int TOP = 2;
    private final int BOTTOM = 3;
    private final int RIGHT = 4;
    private final int LEFT = 5;

    private HandlerThread stream_thread,top_thread, bottom_thread, left_thread, right_thread;
    private Handler stream_handler,top_handler, bottom_handler, left_handler, right_handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);


        live_text = findViewById(R.id.text_1);
        top = findViewById(R.id.top);
        bottom = findViewById(R.id.bottom);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        attack = findViewById(R.id.attack);
        camera_1 = findViewById(R.id.camera_view);

        stream_thread = new HandlerThread("http");
        stream_thread.start();
        stream_handler = new HttpHandler(stream_thread.getLooper());

        top_thread = new HandlerThread("http");
        top_thread.start();
        top_handler = new HttpHandler(top_thread.getLooper());

        bottom_thread = new HandlerThread("http");
        bottom_thread.start();
        bottom_handler = new HttpHandler(bottom_thread.getLooper());

        left_thread = new HandlerThread("http");
        left_thread.start();
        left_handler = new HttpHandler(left_thread.getLooper());

        right_thread = new HandlerThread("http");
        right_thread.start();
        right_handler = new HttpHandler(right_thread.getLooper());


        set_btn(top);
        set_btn(bottom);
        set_btn(right);
        set_btn(left);
        set_btn(attack);
        stream_handler.sendEmptyMessage(CAMERA);
    }

    private class HttpHandler extends Handler
    {
        public HttpHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CAMERA:
                    VideoStream();
                    break;
                case TOP:
                    output_data("cmd", "top");
                    break;
                case BOTTOM:
                    output_data("cmd", "bottom");
                    break;
                case RIGHT:
                    output_data("cmd", "right");
                    break;
                case LEFT:
                    output_data("cmd", "left");
                    break;

                default:
                    break;
            }
        }
    }

    void set_btn(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn == top)                     top_handler.sendEmptyMessage(TOP);
                else if (btn == bottom)             bottom_handler.sendEmptyMessage(BOTTOM);
                else if (btn == right)              right_handler.sendEmptyMessage(RIGHT);
                else if (btn == left)               left_handler.sendEmptyMessage(LEFT);

            }
        });
    }


    void output_data(String to, String cmd){
        try{
            String rssi_url = url + "/"+to+"?val="+cmd;
            try{
                URL url = new URL(rssi_url);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setDoInput(true);
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 5);
                huc.setReadTimeout(1000 * 5);
                huc.connect();

                if (huc.getResponseCode() == 200) {
                    InputStream in = huc.getInputStream();

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(cmd);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void VideoStream()
    {
        String stream_url = url + ":81/camera";
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try
        {
            URL url = new URL(stream_url);
            try
            {
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 10);
                huc.setReadTimeout(1000 * 10);
                huc.setDoInput(true);
                huc.connect();

                if (huc.getResponseCode() == 200)
                {
                    InputStream in = huc.getInputStream();

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);

                    String data;

                    int len;
                    byte[] buffer;

                    while ((data = br.readLine()) != null)
                    {
                        if (data.contains("Content-Type:"))
                        {
                            data = br.readLine();

                            len = Integer.parseInt(data.split(":")[1].trim());

                            bis = new BufferedInputStream(in);
                            buffer = new byte[len];

                            int t = 0;
                            while (t < len)
                            {
                                t += bis.read(buffer, t, len - t);
                            }

                            Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                            final Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    camera_1.setImageBitmap(bitmap);
                                }
                            });

                        }
                    }
                }


            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (bis != null)
                {
                    bis.close();
                }
                if (fos != null)
                {
                    fos.close();
                }

                //stream_handler.sendEmptyMessageDelayed(ID_CONNECT,3000);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }


    private void Bytes2ImageFile(byte[] bytes, String fileName)
    {
        try
        {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }






}