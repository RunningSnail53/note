package edu.hebut.retrofittest.Util;

import android.icu.util.ChineseCalendar;
import android.os.Build;

public class DateUtils {
    public static String getLunarDate(long timestamp) {
        ChineseCalendar chineseCalendar = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            chineseCalendar = new ChineseCalendar();
            chineseCalendar.setTimeInMillis(timestamp);

            int lunarYear = chineseCalendar.get(ChineseCalendar.EXTENDED_YEAR) - 2637;
            int lunarMonth = chineseCalendar.get(ChineseCalendar.MONTH) + 1;
            int lunarDay = chineseCalendar.get(ChineseCalendar.DAY_OF_MONTH);

            // 处理月份显示（例如：一月 → 正月）
//            String monthStr = (lunarMonth == 1 ? "正" : String.valueOf(lunarMonth)) + "月";
//            return String.format("农历%d年%s初%d", lunarYear, monthStr, lunarDay);
            return lunarYear + "-" + lunarMonth + "-" + lunarDay;
        }
        return "";
    }
}
