package edu.hebut.retrofittest.UI.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.DateAxisValueFormatter;
import edu.hebut.retrofittest.Util.SharedDataUtils;
import edu.hebut.retrofittest.chat.client.RetrofitClient;
import edu.hebut.retrofittest.chat.service.ChatApi;
import edu.hebut.retrofittest.supabase.entity.MindName;
import edu.hebut.retrofittest.supabase.entity.MindRecord;
import io.noties.markwon.Markwon;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisFragment extends Fragment {

    private ScrollView scAnalysis;
    private LineChart lineChart;
    private RadarChart radarChart;
    private List<MindRecord> mindRecordList;
    private TextView tvSummary;
    private ChatApi chatApi;
    private Markwon markwon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fgAnalysis = LayoutInflater.from(getContext()).inflate(R.layout.fragment_analysis, null);

        mindRecordList = SharedDataUtils.getMindRecordList();

        lineChart = fgAnalysis.findViewById(R.id.lineChart);
        radarChart = fgAnalysis.findViewById(R.id.radarChart);
        tvSummary = fgAnalysis.findViewById(R.id.tv_summary);
        scAnalysis = fgAnalysis.findViewById(R.id.sc_analysis);
        setupLineChart();
        setupRadarChart();

        chatApi = RetrofitClient.getInstance().create(ChatApi.class);
        markwon = Markwon.builder(getContext()).build();

        return fgAnalysis;
    }

    public void getSummary() {
        tvSummary.setText("Ai总结中...");
        scAnalysis.smoothScrollTo(0, 0);
        chatApi.getSummaryStream(Map.of("userId", SharedDataUtils.getLoginUser().getId()))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("225188", "返回成功");
                            // 使用后台线程处理流数据
                            new Thread(() -> {
                                try (BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(response.body().source().inputStream(), StandardCharsets.UTF_8))) {
                                    Log.d("225188", "流创建成功");

                                    StringBuilder contentBuilder = new StringBuilder();
                                    String line;

                                    while ((line = reader.readLine()) != null) {
                                        if (line != null) {
                                            line += "\n";
                                            Log.d("225188", "onResponse: " + line);
                                            // 处理增量数据并更新UI
                                            processDisplay(line, contentBuilder, tvSummary);
                                        }
                                    }
                                } catch (IOException e) {
                                    Log.d("225188", "流解析失败", e);
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("225188", "请求失败", t);
                    }
                });
    }

    private void setupLineChart() {
        // 空数据监测
        if (lineChart == null) return;
        if (mindRecordList == null || mindRecordList.isEmpty()) {
            lineChart.setNoDataText("暂无情绪记录数据");
            lineChart.setNoDataTextColor(Color.GRAY);
            lineChart.invalidate();
            return;
        }

        // 1. 准备数据（时间戳 + 分数）
        List<Entry> entries = new ArrayList<>();
        for (MindRecord record : mindRecordList) {
            long timestamp = getTimeInMillis(record.getYear(), record.getMonth(), record.getDay());
            entries.add(new Entry(timestamp, record.getScore()));
        }

        // 2. 创建数据集
        LineDataSet dataSet = new LineDataSet(entries, "每日情绪指数");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#90f0b2"));
        dataSet.setFillAlpha(100);

        // 3. 绑定数据到图表
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 4. 配置 X 轴（时间轴）
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new DateAxisValueFormatter()); // 设置时间格式化
        xAxis.setGranularity(1f); // 最小间隔为1单位（1天）
        xAxis.setLabelCount(3); // 显示的标签数量

        // 5. 配置 Y 轴（分数 0-100）
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getAxisRight().setEnabled(false); // 禁用右侧Y轴

        // 6. 其他样式设置
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(1000); // 动画效果
        lineChart.invalidate(); // 刷新图表
    }

    private void setupRadarChart() {
        // 1. 空数据监测
        if (radarChart == null) return;
        if (mindRecordList == null || mindRecordList.isEmpty()) {
            radarChart.setNoDataText("暂无情绪记录数据");
            radarChart.setNoDataTextColor(Color.GRAY);
            radarChart.invalidate();
            return;
        }

        // 2. 使用HashMap统计（Key为String类型）
        Map<String, Integer> mindCounts = new HashMap<>();
        // 初始化所有情绪为0
        for (MindName mind : MindName.values()) {
            mindCounts.put(String.valueOf(mind), 0);
        }

        // 3. 统计情绪出现次数
        for (MindRecord record : mindRecordList) {
            if (record != null && record.getName() != null) {
                String moodName = String.valueOf(record.getName());
                mindCounts.put(moodName, mindCounts.getOrDefault(moodName, 0) + 1);
            }
        }

        // 4. 准备雷达图数据
        List<RadarEntry> entries = new ArrayList<>();
        for (MindName mind : MindName.values()) {
            entries.add(new RadarEntry(
                    mindCounts.get(String.valueOf(mind)), // 获取对应情绪的计数
                    String.valueOf(mind) // 使用String.valueOf转换枚举名称
            ));
        }

        // 5. 创建数据集
        RadarDataSet dataSet = new RadarDataSet(entries, "情绪统计");
        dataSet.setColor(Color.parseColor("#FF6F00")); // 橙色
        dataSet.setFillColor(Color.parseColor("#FFE082")); // 浅橙色填充
        dataSet.setDrawFilled(true);
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(2f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // 6. 绑定数据
        RadarData radarData = new RadarData(dataSet);
        radarChart.setData(radarData);

        // 7. 配置X轴（显示中文标签）
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value % MindName.values().length;
                return String.valueOf(MindName.values()[index]); // 使用String.valueOf
            }
        });

        // 8. 配置Y轴
        int maxCount = Collections.max(mindCounts.values());
        radarChart.getYAxis().setAxisMinimum(0f);
        radarChart.getYAxis().setAxisMaximum(maxCount + 1);

        // 9. 其他样式设置
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.LTGRAY);
        radarChart.getDescription().setEnabled(false);
        radarChart.animateY(1000);
        radarChart.invalidate();
    }

    public void playChartAnimations() {
        if (lineChart != null) {
            lineChart.animateY(1000, Easing.EaseInOutQuad);
        }
        if (radarChart != null) {
            radarChart.animateY(1200, Easing.EaseOutBack);
        }
    }

    // 将年月日转换为时间戳（毫秒）
    private long getTimeInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // 注意：月份从0开始
        return calendar.getTimeInMillis();
    }

    private void processDisplay(String delta, StringBuilder contentBuilder, TextView textView) {
        for (char c : delta.toCharArray()) {
            contentBuilder.append(c);
            final String finalContent = contentBuilder.toString();

            // 在主线程更新UI
            new Handler(Looper.getMainLooper()).post(() -> {

                markwon.setMarkdown(tvSummary, finalContent);
                scAnalysis.smoothScrollTo(0, scAnalysis.getBottom());
            });

            // 控制显示速度（在后台线程睡眠）
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
