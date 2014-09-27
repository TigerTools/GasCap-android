package tools.tiger.gascap.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by jgreathouse on 9/18/2014.
 */

public class GasApp extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        GasApp.context = getApplicationContext();
        init();
    }

    public static Context getAppContext() {
        return GasApp.context;
    }


    private void init() {
        GasClient.init(this);
    }
}
