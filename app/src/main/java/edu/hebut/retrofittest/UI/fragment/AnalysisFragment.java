package edu.hebut.retrofittest.UI.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import java.util.ArrayList;
import java.util.List;

import edu.hebut.retrofittest.R;

public class AnalysisFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fgAnalysis = LayoutInflater.from(getContext()).inflate(R.layout.fragment_analysis, null);

        LineChart lineChart = fgAnalysis.findViewById(R.id.lineChart);
        RadarChart radarChart = fgAnalysis.findViewById(R.id.radarChart);

        // 设置 LineChart 的模拟数据
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4));
        entries.add(new Entry(1, 2));
        entries.add(new Entry(2, 6));
        entries.add(new Entry(3, 8));
        entries.add(new Entry(4, 3));

        LineDataSet dataSet = new LineDataSet(entries, "折线图"); // 设置线的名称
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.RED); // 设置线的颜色
        dataSet.setLineWidth(2f); // 设置线的宽度

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.invalidate(); // 刷新图表视图



    // 设置 RadarChart 的模拟数据
        List<RadarEntry> radarEntries = new ArrayList<>();
        radarEntries.add(new RadarEntry(4));
        radarEntries.add(new RadarEntry(6));
        radarEntries.add(new RadarEntry(8));
        radarEntries.add(new RadarEntry(2));
        radarEntries.add(new RadarEntry(5));

        RadarDataSet radarDataSet = new RadarDataSet(radarEntries, "雷达图");
        radarDataSet.setColor(Color.BLUE);
        radarDataSet.setFillColor(Color.CYAN);
        radarDataSet.setDrawFilled(true);
        radarDataSet.setLineWidth(2f);

       RadarData radarData = new RadarData(radarDataSet);
        radarChart.setData(radarData);

// 刷新 RadarChart 视图
        radarChart.invalidate();

        return fgAnalysis;
    }
}
