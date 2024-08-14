package top.moye.miraibotwatch;

import android.content.Context;
import android.content.res.Configuration;

public class AppTool {
    public static void setDefaultDisplay(Context context) {
        Configuration origConfig = context.getResources().getConfiguration();
        origConfig.densityDpi = 320;
        context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics());
    }
}
