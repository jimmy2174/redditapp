package com.app.appydinos.redditscraper.PostWrapperActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.app.appydinos.redditscraper.RedditItemDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by james on 23-Apr-17.
 */

public class PostActivityViewLogic {
    private Context mContext;
    private PostActivity mCaller;
    private ProgressDialog mProgressDialog;
    private String mJSONString;

    protected void uiInitilised(@NonNull Context context, @NonNull PostActivity caller) {
        mCaller = caller;
        mContext = context;
    }

    public void getJSONData(String sub, String order) {
        mCaller.setTitle(sub);
        String url = String.format("https://www.reddit.com%s.json", sub);
        new JsonTask().execute(url);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(mCaller);
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mJSONString = result;
            parseJSONFile();
        }
    }

    private void parseJSONFile() {
        RedditItemDTO redditPost = new RedditItemDTO();
        if (mJSONString != null) {
            try {
                JSONArray jsonArray = new JSONArray(mJSONString);
//                for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(0);
                // Getting JSON Array node
                JSONObject object = jsonObj.getJSONObject("data");
                JSONArray children = object.getJSONArray("children");

                JSONObject p = children.getJSONObject(0);
                if (p.get("data") instanceof JSONObject) {
                    JSONObject obj = (JSONObject) p.get("data");
                    String title = obj.getString("title");
                    String score = obj.getString("score");
                    String author = obj.getString("author");
                    String content = obj.getString("selftext");
                    String comments = obj.getString("num_comments");
                    Date df = new java.util.Date(obj.getLong("created_utc") * 1000);
                    String formattedDate = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);


                    RedditItemDTO thisPost = new RedditItemDTO();
                    thisPost.titleText = title;
                    thisPost.points = score;
                    thisPost.user = "Author: " + author;
                    thisPost.contentText = content;
                    thisPost.comments = comments;
                    thisPost.timeSincePost = formattedDate;
                    redditPost = thisPost;
                }


            } catch (final JSONException e) {
                Log.e("JSON PARSER", "Json parsing error: " + e.getMessage());
                mCaller.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mCaller, "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        } else {
            Log.e("JSON PARSER", "Couldn't get json from server.");
            mCaller.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCaller, "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                }
            });

        }

        mCaller.populateScreen(redditPost);

        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}