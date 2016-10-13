package com.empire.android.dinnertonight;

import java.util.Date;

/**
 * Created by lstanzione on 10/7/2016.
 */
public class Util {

    public static long getTimestamp(){

        Date nowDate = new Date();
        return nowDate.getTime();

    }

}
