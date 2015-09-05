package it.sellainbox.connection;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by GodwinRoseSamuel on 06-Sep-15.
 */
public class HttpClientManager {
    private static final String TAG = "HttpClientManager";
    private static final HttpClient httpClient = new DefaultHttpClient();
    private HttpPost httpPost;

    public String getHttpPostResponse(final String url,final String input) {
        StringBuffer result = new StringBuffer();
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(input));

            HttpResponse response = httpClient.execute(httpPost);

            Log.e(TAG, "Response Code : " + response.getStatusLine().getStatusCode());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            Log.e(TAG, "Result : " + result);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
