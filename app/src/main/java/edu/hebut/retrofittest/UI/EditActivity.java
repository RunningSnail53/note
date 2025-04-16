package edu.hebut.retrofittest.UI;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.text.EmojiCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.hebut.retrofittest.Adapter.PicAdapter;
import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.DB.NoteDao;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.DateUtils;
import com.yanzhenjie.album.AlbumFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private RecyclerView rvPic;
    private PicAdapter adapter;
    private ImageView ivMind;
    private EditText et_new_title;
    private EditText et_new_content;
    private Spinner spinner,spMind;
    private NoteDao noteDao;
    private NoteBean note;
    private int myID;
    private String myTitle;
    private String myContent;
    private String myCreate_time;
    private String myUpdate_time;
    private String myType;
    private int mark;
    private Calendar calendar;
    private String login_user;
    private int flag;//区分是新建还是修改
    private MediaRecorder mMediaRecorder;
    private Button btnPlay;
    private String mOutputFilePath;
    private int musicRawId;
    private Button btnRecord;
    private Button btnSelectMusic,btnPlayMusic;
    private EditText edtLink;
    private TextView tvLink;
    private MediaPlayer mediaPlayer;
    private int weather = -1;
    private Spinner spWeather;
    private ImageView ivSeason;
    private int[] minds = {R.mipmap.mind0,R.mipmap.mind1,R.mipmap.mind2,R.mipmap.mind3,R.mipmap.mind4,R.mipmap.mind5};
    private String[] emojis = {"\u2600", "\u26C5", "\uD83C\uDF27\uFE0F", "\uD83C\uDF28\uFE0F", "\uD83C\uDF2C\uFE0F"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        init();
        getNowTime();
        initEvent();

    }

    public interface OnImageUploadListener {
        void onUploadStart();
        void onUploadSuccess(String url);
        void onUploadFailure(Exception e);
    }


    private void initEvent() {
        spMind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mark = minds[i];
                setMindImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spWeather.setSelection(0,true);
        spWeather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    weather = i-1;
                    addWeatherEmoji();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnRecord.getTag() == null) {
                    btnRecord.setTag(1);
                    btnRecord.setText("停止录制");
                    btnPlay.setVisibility(View.GONE);
                    initMediaRecorder();
                    startRecording();
                }else {
                    btnRecord.setTag(null);
                    btnRecord.setText("开始录制");
                    btnPlay.setVisibility(View.VISIBLE);
                    stopRecording();
                }
            }
        });
        btnSelectMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditActivity.this, MusicSelectionActivity.class);
                startActivityForResult(intent,1);
            }
        });
        btnPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (( btnPlayMusic.getTag()) !=null) {
                    stopMusic();
                    btnPlayMusic.setTag(null);
                    btnPlayMusic.setText("播放音乐");
                }else {
                    playMusic();
                    btnPlayMusic.setTag(1);
                    btnPlayMusic.setText("停止音乐");
                }
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mOutputFilePath)) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(mOutputFilePath);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
                                btnPlay.setVisibility(View.GONE);
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                btnPlay.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        btnPlay.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void addWeatherEmoji() {
        CharSequence processed = EmojiCompat.get().process(emojis[weather]);
        String s = processed.toString();
        et_new_content.setText(s+s+s+et_new_content.getText().toString());
    }


    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    // 音频源为麦克风
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置输出文件路径（示例路径）
        mOutputFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/recording"+System.currentTimeMillis()+".mp3";
        mMediaRecorder.setOutputFile(mOutputFilePath);
    }
    private void startRecording() {
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Toast.makeText(this, "录音开始", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "录音初始化失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopRecording() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Toast.makeText(this, "录音已保存至：" + mOutputFilePath, Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "停止录音失败", Toast.LENGTH_SHORT).show();
        }
    }


    private void selectTime() {


        calendar = Calendar.getInstance();
        DatePickerDialog dpdialog = new DatePickerDialog(EditActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int month, int day) {
                        // TODO Auto-generated method stub
                        // 更新EditText控件日期 小于10加0
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));


        final TimePickerDialog tpdialog = new TimePickerDialog(EditActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR, i);
                calendar.set(Calendar.MINUTE, i1);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            }
        }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
        dpdialog.show();
        dpdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                tpdialog.show();
            }
        });
    }

    private void  init() {
        ivSeason = findViewById(R.id.ivSeason);
        ivMind = findViewById(R.id.ivMind);
        btnPlay = findViewById(R.id.btnPlay);
        edtLink = findViewById(R.id.edtLink);
        tvLink = findViewById(R.id.tvLink);
        btnSelectMusic = findViewById(R.id.btnSelectMusic);
        btnPlayMusic = findViewById(R.id.btnPlayMusic);
        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        login_user = intent.getStringExtra("login_user");
        adapter = new PicAdapter(this);
        rvPic = findViewById(R.id.rvPic);
        rvPic.setLayoutManager(new GridLayoutManager(this, 4));
        rvPic.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 5;
                outRect.right = 5;
                outRect.top = 5;
                outRect.bottom = 5;
            }
        });
        rvPic.setAdapter(adapter);
        int month = Integer.parseInt(DateUtils.getLunarDate(System.currentTimeMillis()).split("-")[1]);
        if (month <= 3) {
            ivSeason.setImageResource(R.mipmap.spring);
        } else if (month <= 6) {
            ivSeason.setImageResource(R.mipmap.summer);
        } else if (month <= 9) {
            ivSeason.setImageResource(R.mipmap.fall);
        } else if (month <= 12) {
            ivSeason.setImageResource(R.mipmap.winter);
        }
        et_new_title = (EditText) findViewById(R.id.et_new_title);
        et_new_content = (EditText) findViewById(R.id.et_new_content);
        spinner = (Spinner) findViewById(R.id.type_select);
        spMind = findViewById(R.id.spMind);
        spWeather = findViewById(R.id.spWeather);
        btnRecord = findViewById(R.id.btnRecord);

        if (flag == 0) {//0新建
            setTitle("新建记事");
            myCreate_time = getNowTime();
            myUpdate_time = getNowTime();

        } else if (flag == 1) {//1编辑
            Bundle bundle = intent.getBundleExtra("data");
            note = (NoteBean) bundle.getSerializable("note");
            myID = note.getId();
            myTitle = note.getTitle();
            myContent = note.getContent();
            myCreate_time = note.getCreateTime();
            myUpdate_time = note.getUpdateTime();
            login_user = note.getOwner();
            myType = note.getType();
            mark = note.getMark();
            weather = note.getWeather();
            setTitle("编辑记事");
            for (int i = 0; i < 5; i++) {
                if (spinner.getItemAtPosition(i).toString().equals(myType)) {
                    spinner.setSelection(i);
                }
            }
            for (int j = 0; j < 7; j++) {
                if (mark == j) {
                    spMind.setSelection(j);
                    setMindImage();
                }
            }
            et_new_title.setText(note.getTitle());
            et_new_content.setText(note.getContent());
            musicRawId = note.getMusicId();
            mOutputFilePath = note.getRecordPath();
            edtLink.setText(note.getLink());
            if (!TextUtils.isEmpty(mOutputFilePath)) {
                btnPlay.setVisibility(View.VISIBLE);
            }
            if (musicRawId != 0) {
                btnPlayMusic.setVisibility(View.VISIBLE);
            }
        }

        if (flag == 0) {
            ArrayList<AlbumFile> l = new ArrayList<>();
            l.add(null);
            adapter.setNewData(l);
        } else {
            ArrayList<AlbumFile> list = new ArrayList<>();
            try {
                for (int k = 0; k < note.getPicPaths().size(); k++) {
                    AlbumFile albumFile = new AlbumFile();
                    albumFile.setPath(note.getPicPaths().get(k));
                    list.add(albumFile);
                };
                list.add(null);
                adapter.setNewData(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setMindImage() {
        ivMind.setImageResource(minds[spMind.getSelectedItemPosition()]);
    }

    private String getNowTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNowStr = sdf.format(d);
        return dateNowStr;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_new_save) {
            saveNoteDate();
        } else if (itemId == R.id.action_new_giveup) {
            finish();
        } else {
            // 未识别的菜单项交给父类处理
            return super.onOptionsItemSelected(item);
        }

        return true; // 明确表示已处理事件
    }

    private void saveNoteDate() {

        String noteTitle = et_new_title.getText().toString();
        if (noteTitle.length() > 14) {
            Toast.makeText(EditActivity.this, "标题长度应在15字以下", Toast.LENGTH_SHORT).show();
            return;
        } else if (noteTitle.isEmpty()) {
            Toast.makeText(EditActivity.this, "标题内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String noteContent = et_new_content.getText().toString();
        if (noteContent.isEmpty()) {
            Toast.makeText(EditActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String notecreateTime = myCreate_time;
        String noteupdateTime = getNowTime();
        int noteID = myID;
        if (note == null) {
            note = new NoteBean();
        }
        noteDao = new NoteDao();
        note.setTitle(noteTitle);
        note.setContent(noteContent);


//        Log.e("HHH", "emoji " + noteContent);
//        Toast.makeText(this, noteContent+"", Toast.LENGTH_SHORT).show();
        if (flag == 0) {
            note.setCreateTime(notecreateTime);
            note.setUpdateTime(noteupdateTime);
        }else {
            note.setUpdateTime(noteupdateTime);
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        note.setYear(calendar.get(Calendar.YEAR) + "");
        note.setMonth((calendar.get(Calendar.MONTH) + 1) + "");
        note.setDay(calendar.get(Calendar.DAY_OF_MONTH) + "");
        note.setType(spinner.getSelectedItem().toString());
        note.setOwner(login_user);
        note.setMark(mark);
        note.setRecordPath(mOutputFilePath);
        note.setMusicId(musicRawId);
        note.setLink(edtLink.getText().toString().trim());
        note.setWeather(weather);
        List<String> filePaths = new ArrayList<>();
        for (int j = 0; j < adapter.getmList().size(); j++) {
            if (adapter.getmList().get(j) != null) {
                filePaths.add(adapter.getmList().get(j).getPath());
            }
        }
        note.setPicPaths(filePaths);
        if (flag == 0) {//新建记事
            noteDao.insertNote(note);
        } else if (flag == 1) {//修改记事
            note.setId(noteID);
            noteDao.updateNote(note);
        }

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnPlayMusic.setVisibility(View.GONE);
        if (requestCode == 1) {
            if (resultCode == 1) {
                try {
                    musicRawId = Integer.parseInt(MusicSelectionActivity.music);
                    btnPlayMusic.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    btnPlayMusic.setVisibility(View.GONE);
                }
            }
        }
    }

    private void playMusic() {
        if (musicRawId == 0) {
            return;
        }
        mediaPlayer = MediaPlayer.create(this, musicRawId);
        mediaPlayer.start();
    }

    private void stopMusic() {
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

