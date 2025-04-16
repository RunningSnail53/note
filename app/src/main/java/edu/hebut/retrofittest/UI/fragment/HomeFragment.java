package edu.hebut.retrofittest.UI.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.hebut.retrofittest.Adapter.NoteListAdapter;

import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.DB.NoteDao;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.UI.EditActivity;
import edu.hebut.retrofittest.Util.DateUtils;
import edu.hebut.retrofittest.Util.SharedDataUtils;
import edu.hebut.retrofittest.supabase.dao.NoteDaoKt;
import edu.hebut.retrofittest.supabase.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    public List<NoteBean> noteList;
    public static String login_user;
    private ListView lvData;
    private Button btnAdd;
    private NoteListAdapter adapter;
    private TextView tvDate;
    private TextView tvReset;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, null);
        tvDate = v.findViewById(R.id.tvDate);
        btnAdd = v.findViewById(R.id.btnAdd);
        login_user = SharedDataUtils.getLoginUser().getName();
        lvData = v.findViewById(R.id.lvData);
        adapter = new NoteListAdapter();
        tvReset = v.findViewById(R.id.tvReset);
        lvData.setAdapter(adapter);
        btnAdd = v.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), EditActivity.class);
            intent.putExtra("flag", 0);
            intent.putExtra("login_user", login_user);
            startActivity(intent);
        });
        tvDate.setOnClickListener(view -> new DatePickerDialog(getContext(),
                (datePicker, i, i1, i2) -> {
                    String month = i1 < 10 ? "0" + ++i1 : ++i1 + "";
                    String day = i2 < 10 ? "0" + i2 : i2 + "";
                    tvDate.setText(i + "-" + month + "-" + day);
                    tvDate.setTag(tvDate.getText().toString().trim());
                    initData();
                }, 2025, 2, 1).show());
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDate.setText("请选择日期");
                tvDate.setTag(null);
                initData();
            }
        });
        initData();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }



    public void initData() {


        if (tvDate.getTag() == null) {
            noteList = SharedDataUtils.getNoteBeanList();
        } else {
            noteList = SharedDataUtils.getNoteBeanList().stream().filter(noteBean -> {
                String date = noteBean.getYear() + "-" + noteBean.getMonth() + "-" + noteBean.getDay();
                return date.equals(tvDate.getTag().toString());
            }).collect(Collectors.toList());
        }
        adapter.setmNotes(noteList);
    }

}
