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
    private ResultsActivity mActivity;
    private ImageView mImageView;
    private String mImageUrl;
    private final int WIDTH;
    private final int HEIGHT;

    public ImageLoadTask(ResultsActivity activity, ImageView imageView, String imageUrl, int width, int height) {
        mActivity = activity;
        mImageView = imageView;
        mImageUrl = imageUrl;
        WIDTH = width;
        HEIGHT = height;
    }

    @Override
    protected Bitmap doInBackground(Void... params){
        try{ // get new bitmap from the net
            String width = "164";//"82";
            String height = "236";//"118";
            //Download fixed sized images, and scale them to needed size later
            URL urlConnection = NetworkUtils.buildImageUrl(mImageUrl, width, height);
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
        if(bitmap != null){
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
            mImageView.setImageBitmap(scaledBitmap);
            mActivity.addBitmapToMemoryCache(mImageUrl, bitmap);
        }
    }
}
