package edu.hebut.retrofittest.Util;

import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {
    private final SimpleDateFormat mFormat;

    public DateAxisValueFormatter() {
        mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        // value 是时间戳（单位：毫秒）
        return mFormat.format(new Date((long) value));
    }
}