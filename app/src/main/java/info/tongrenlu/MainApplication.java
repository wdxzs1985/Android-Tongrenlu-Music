package info.tongrenlu;

import android.app.Application;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by wangjue on 2015/04/02.
 */
public class MainApplication extends Application {

    private OkHttpClient mClient;

    public static OkHttpClient getHttpClient(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext instanceof MainApplication) {
            return ((MainApplication) applicationContext).mClient;
        }
        return new OkHttpClient();
    }

    private static OkHttpClient initHttpClient() {
        OkHttpClient client = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);
        return client;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClient = initHttpClient();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
