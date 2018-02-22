package com.sergeyrodin.onlinelifeviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 12.05.16.
 */
public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private String url;
    private Result result;
    private ImageView imageView;
    public ImageLoadTask(String url, ImageView imageView){
        this.url = url;
        this.imageView = imageView;
    }

    public ImageLoadTask(Result result, ImageView imageView) {
        this.result = result;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params){
        try{ // get new bitmap from the net
            URL urlConnection = new URL(result.image);
            HttpURLConnection connection = (HttpURLConnection)urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }catch(Exception e){
            System.err.println(e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        if(bitmap == null){
            //imageView.setImageResource(R.drawable.ic_action_link);
        }else{
            imageView.setImageBitmap(bitmap);
            result.setBitmap(bitmap);
        }
    }
}
