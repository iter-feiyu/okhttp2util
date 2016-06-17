package com.sam.demos;

import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.sam.utils.AppContext;
import com.sam.utils.SDCardUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import extension.HttpResult;
import extension.OkHttp2Platform;
import extension.OkHttp2ResultListener;
import extension.ParamHashBuilder;


public class MainActivity extends AppCompatActivity {
    private static final String LOGTAG = MainActivity.class.getSimpleName();

    private Handler handler = new Handler();

    @ViewInject(R.id.main_textviewTV)
    private TextView textView;
    @ViewInject(R.id.main_imageIV)
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"MainActivity onCreate");

        OkHttp2Platform.init(this);

    }


    @OnClick({R.id.main_button1BTN})
    private void onButton1Clicked(View view)
    {
        AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"onButton1Clicked");
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    final HttpResult httpResult = OkHttp2Platform.getSimpleRequest("http://112.124.8.254:8080/testwebpro/test");
                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"get code = "+httpResult.code+",result length = "+httpResult.result.length()+",content:\n"+httpResult.result);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    HttpResult httpResult = OkHttp2Platform.postSimpleRequest("http://112.124.8.254:8080/testwebpro/test",new ParamHashBuilder().addParam("key1","param1"));
                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"post code = "+httpResult.code+"result length = "+httpResult.result.length()+",content:\n"+httpResult.result);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    OkHttp2Platform.downLoadSimpleFile("http://112.124.8.254:8080/testwebpro/images/1.jpg", new File(Environment.getExternalStorageDirectory(), "debug" + System.currentTimeMillis()).getAbsolutePath()
                            , new OkHttp2ResultListener() {
                                @Override
                                public void onProgress(long total, long cur) {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"downLoadSimpleFile total = "+total+",cur = "+cur);
                                }

                                @Override
                                public void onFailed() {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"downLoadSimpleFile onFailed");
                                }

                                @Override
                                public void onSuccess(HttpResult httpResult) {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"downLoadSimpleFile onSuccess");
                                }
                            });

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();


        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    File file = new File(Environment.getExternalStorageDirectory(),"classes.dex");
                    OkHttp2Platform.uploadSimpleFile("http://112.124.8.254:8080/testwebpro/test", null, "classes.dex", file.getAbsolutePath(),
                            new OkHttp2ResultListener() {
                                @Override
                                public void onProgress(long total, long cur) {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"uploadfile total = "+total+",cur = "+cur);
                                }

                                @Override
                                public void onFailed() {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"uploadfile onFailed");
                                }

                                @Override
                                public void onSuccess(HttpResult httpResult) {
                                    AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"uploadfile onSuccess,"+httpResult.result);
                                }
                            });
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    @OnClick(R.id.main_button2BTN)
    private void onButton2Clicked(View view)
    {
        AppContext.REALLOG(Level.DEBUG_INT,LOGTAG,"onButton2Clicked");
    }

}
