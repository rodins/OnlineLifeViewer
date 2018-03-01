package com.sergeyrodin.onlinelifeviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by root on 12.05.16.
 */
public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private final String TAG = ImageLoadTask.class.getSimpleName();
    private Result result;
    private ImageView imageView;
    private final int WIDTH;
    private final int HEIGHT;

    public ImageLoadTask(Result result, ImageView imageView, int width, int height) {
        this.result = result;
        this.imageView = imageView;
        WIDTH = width;
        HEIGHT = height;
    }

    @Override
    protected Bitmap doInBackground(Void... params){
        try{ // get new bitmap from the net
            String width = "164";//"82";
            String height = "236";//"118";
            //Download fixed sized images, and scale them to needed size later
            URL urlConnection = NetworkUtils.buildImageUrl(result.image, width, height);
            HttpURLConnection connection = (HttpURLConnection)urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap downloadedBitmap = BitmapFactory.decodeStream(input);
            return Bitmap.createScaledBitmap(downloadedBitmap, WIDTH, HEIGHT, true);
        }catch(Exception e){
            System.err.println(e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            //TODO: fix out of memory error
            result.setBitmap(bitmap);
        }
    }
}
