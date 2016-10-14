package com.empire.android.dinnertonight;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lstanzione on 10/7/2016.
 */
public class Util {

    public static long getTimestamp(){

        Date nowDate = new Date();
        return nowDate.getTime();

    }

    public static String getCurrentDate(){

        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(nowDate);

    }

    public static String getCurrentDateLabel(){

        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd / MMM / yyyy");
        return sdf.format(nowDate);

    }

}
