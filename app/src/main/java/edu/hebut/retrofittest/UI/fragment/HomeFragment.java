package edu.hebut.retrofittest.UI.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.util.List;

public class HomeFragment extends Fragment {
    private NoteDao noteDao;
    public List<NoteBean> noteList;
    public static String login_user;
    private ListView lvData;
    private Button btnAdd;
    private NoteListAdapter adapter;
    private TextView tvDate;
    private TextView tvReset;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, null);
        tvDate = v.findViewById(R.id.tvDate);
        noteDao = new NoteDao(getContext());
        btnAdd = v.findViewById(R.id.btnAdd);
        login_user = getContext().getSharedPreferences("login", MODE_PRIVATE).getString("login_user", "");
        lvData = v.findViewById(R.id.lvData);
        adapter = new NoteListAdapter();
        tvReset = v.findViewById(R.id.tvReset);
        lvData.setAdapter(adapter);
        btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditActivity.class);
                intent.putExtra("flag", 0);
                intent.putExtra("login_user", login_user);
                startActivity(intent);
            }
        });
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        String month = i1 < 10 ? "0" + ++i1 : ++i1+"";
                        String day = i2 < 10 ? "0" + i2 : i2+"";
                        tvDate.setText(i+"-"+month+"-"+day);
                        tvDate.setTag(tvDate.getText().toString().trim());
                        initData();
                    }
                }, 2025, 2, 1).show();

            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDate.setText("请选择日期");
                tvDate.setTag(null);
                initData();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    //初始化数据库数据
    public void initData() {
        if (tvDate.getTag() == null) {
            noteList = noteDao.queryNotesAll(login_user);
        }else {
            noteList = noteDao.queryNotesByDate(login_user, ((String) tvDate.getTag()));
        }
        adapter.setmNotes(noteList);

    }
}
