/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package android.takephoto.src.main.java.com.pm.takephoto.utils;

import android.content.res.Resources;

public class DimensionUtil {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
