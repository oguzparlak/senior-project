package utils;

import android.app.Application;
import android.os.Build;

/**
 * Created by Oguz on 13/04/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * Use this code when you want to override the default system font
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                FontOverrider.overrideFont(this, "MONOSPACE", "fonts/peace_sans.otf");
         }
         */
    }
}
