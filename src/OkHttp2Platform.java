package extension;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by guoyiyou on 16/6/17.
 */
public class OkHttp2Platform {

    private static OkHttpClient instance = null;

    public static final void init(Context context)
    {
        if(instance == null)
        {
            synchronized (OkHttp2Platform.class)
            {
                instance = new OkHttpClient();
                instance.setConnectTimeout(5, TimeUnit.SECONDS);
                instance.setReadTimeout(5,TimeUnit.SECONDS);
                instance.setCookieHandler(new CookieManager(new PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL));
            }
        }
    }

    private static RequestBody makeEasyFileBody(String filepath)
    {
        return RequestBody.create(MediaType.parse("application/octet-stream"),new File(filepath));
    }

    /**
     * 适用于简单的get请求,返回的是string
     */
    public static final HttpResult getSimpleRequest(String urlWithParams) throws Exception
    {
        HttpResult httpResult = new HttpResult();
        try {
            Request request = new Request.Builder().url(urlWithParams).cacheControl(CacheControl.FORCE_NETWORK).build();
            Response response = instance.newCall(request).execute();
            if(response.isSuccessful())
            {
                httpResult.code = response.code();
                httpResult.result = response.body().string();
            }else{
                throw new Exception("request failed with url = "+urlWithParams);
            }
            response.body().close();
        }catch (Exception e)
        {
            throw e;
        }

        return httpResult;
    }


    /**
     * 简单的post请求，返回的是string
     */
    public static final HttpResult postSimpleRequest(String url,@NonNull ParamHashBuilder paramHashBuilder) throws Exception
    {
        HttpResult httpResult = new HttpResult();
        try {
            HashMap<String,String> hashMap = paramHashBuilder.getAllParams();
            Set<String> keys =  hashMap.keySet();
            FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
            for(String key:keys)
            {
                formEncodingBuilder.add(key,hashMap.get(key));
            }
            RequestBody requestBody = formEncodingBuilder.build();
            Request request = new Request.Builder().url(url).cacheControl(CacheControl.FORCE_NETWORK).post(requestBody).build();
            Response response = instance.newCall(request).execute();
            if(response.isSuccessful())
            {
                httpResult.code = response.code();
                httpResult.result = response.body().string();
            }else{
                throw new Exception("request failed with url = "+url);
            }
            response.body().close();
        }catch (Exception e)
        {
            throw e;
        }
        return httpResult;
    }

    /**
     * 下载一个文件
     */
    public static final void downLoadSimpleFile(String urlWithParams,String downloadToPath,OkHttp2ResultListener listener)
    {
        try {
            Request request = new Request.Builder().url(urlWithParams).cacheControl(CacheControl.FORCE_NETWORK).build();
            Response response = instance.newCall(request).execute();
            if(!response.isSuccessful())
            {
                if(listener != null)
                {
                    listener.onFailed();
                }
                return;
            }
            InputStream inputStream =  response.body().byteStream();
            byte buffer[] = new byte[5000];
            long contentLength = response.body().contentLength();
            int totalReadLength = 0;
            int readBytes = 0;
            File file = new File(downloadToPath);
            if(file.exists())
            {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            while ((readBytes = inputStream.read(buffer)) > 0)
            {
                totalReadLength+=readBytes;
                fileOutputStream.write(buffer,0,readBytes);
                if(listener != null)
                {
                    listener.onProgress(contentLength,totalReadLength);
                }
            }
            fileOutputStream.close();
            if(totalReadLength == contentLength)
            {
                if(listener != null)
                {
                    HttpResult httpResult = new HttpResult();
                    httpResult.result = downloadToPath;
                    httpResult.code = 200;
                    listener.onSuccess(httpResult);
                }
            }else{
                if(listener != null)
                {
                    listener.onFailed();
                }
            }
            response.body().close();
        }catch (Exception e)
        {
            if(listener != null)
            {
                listener.onFailed();
            }
        }
    }

    /**
     * 上传一个简单的文件,这里onsuccess里返回的是file路径
     */
    public static final void uploadSimpleFile(String url, @Nullable ParamHashBuilder otherParams, String filekey, String uploadFilePath
            , OkHttp2ResultListener listener)
    {
        HttpResult httpResult = new HttpResult();
        try {
            File file = new File(uploadFilePath);
            if(!file.exists() || file.isDirectory())
            {
                throw new Exception("file is dir or not exist!");
            }
            ProgressedMultipartBuilder progressedMultipartBuilder = new ProgressedMultipartBuilder();
            if(otherParams != null)
            {
                HashMap<String,String> hashMap = otherParams.getAllParams();
                Set<String> keys =  hashMap.keySet();
                for(String key:keys)
                {
                    progressedMultipartBuilder.addFormDataPart(key,hashMap.get(key));
                }
            }
            progressedMultipartBuilder.addFormDataPart(filekey,file.getName(),makeEasyFileBody(uploadFilePath));
            ProgressedMultipartRequestBody requestBody = progressedMultipartBuilder.build();
            requestBody.setProgressListener(listener);

            Request request = new Request.Builder().url(url)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .post(requestBody)
                    .build();

            Response response = instance.newCall(request).execute();
            if(response.isSuccessful())
            {
                httpResult.code = response.code();
                httpResult.result = response.body().string();
                if(listener != null)
                {
                    listener.onSuccess(httpResult);
                }
            }else{
                throw new Exception("request failed with url = "+url);
            }
            response.body().close();
        }catch (Exception e)
        {
            if(listener != null)
            {
                listener.onFailed();
            }
        }
    }




}
