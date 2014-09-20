package tools.tiger.gascap.app;
import android.app.Application;

/**
 * Created by jgreathouse on 9/18/2014.
 */

public class App_Gas  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }


    private void init() {
        GasClient.init(this);
    }
}
