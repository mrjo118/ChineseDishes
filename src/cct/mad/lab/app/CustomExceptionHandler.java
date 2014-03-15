package cct.mad.lab.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

/* 	On the crashing activity, in the onCreate method add this line below super.onCreate(savedInstanceState):
Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

In the LogCat view in eclipse, filter by 'Tag' and see the errors
*/

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    public CustomExceptionHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        Log.d("Tag", "uncaughtException", e);
        defaultUEH.uncaughtException(t, e);
    }
}
