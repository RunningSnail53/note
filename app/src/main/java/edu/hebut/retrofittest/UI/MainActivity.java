package edu.hebut.retrofittest.UI;

//import static com.dxxy.note.service.MyIntentService.ACTION_FOO;
//import static com.dxxy.note.service.MyIntentService.EXTRA_PARAM1;
//import static com.dxxy.note.service.MyIntentService.EXTRA_PARAM2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.UI.fragment.AnalysisFragment;
import edu.hebut.retrofittest.UI.fragment.DecideFragment;
import edu.hebut.retrofittest.UI.fragment.HomeFragment;
import edu.hebut.retrofittest.UI.fragment.MomentsFragment;
import edu.hebut.retrofittest.Util.SharedDataUtils;
import edu.hebut.retrofittest.supabase.dao.UserDao;
import edu.hebut.retrofittest.supabase.entity.User;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String login_user;
    private static String path = "/sdcard/Memo/";// sd路径
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    private ViewPager vpMain;
    private final List<Fragment> fragments = new ArrayList<>();
    private final TextView[] tvOptions = new TextView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvOptions[0] = findViewById(R.id.tvNote);
        tvOptions[1] = findViewById(R.id.tvAnalysis);
        tvOptions[2] = findViewById(R.id.tvDevice);
        tvOptions[3] = findViewById(R.id.tvMoment);
        login_user = SharedDataUtils.getLoginUser().getName();
//        nav_selected = 2;
        setTitle(login_user + "的记事本");
        vpMain = findViewById(R.id.vpMain);
        fragments.add(new HomeFragment());
        fragments.add(new AnalysisFragment());
        fragments.add(new DecideFragment());
        fragments.add(new MomentsFragment());
        vpMain.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
        vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                resetTabs();
                tvOptions[i].setBackgroundResource(R.drawable.shape_bg);
                tvOptions[i].setTextColor(Color.parseColor("#FFFFFF"));
                if (i == 1) {
                    ((AnalysisFragment) fragments.get(i)).getSummary();
                    ((AnalysisFragment) fragments.get(i)).playChartAnimations();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        for (int i = 0; i < 4; i++) {
            final int finalI = i;
            tvOptions[i].setOnClickListener(view -> {
                resetTabs();
                vpMain.setCurrentItem(finalI, true);
            });
        }
        tvOptions[0].setBackgroundResource(R.drawable.shape_bg);
        tvOptions[0].setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void resetTabs() {
        for (int i = 0; i < 4; i++) {
            tvOptions[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
            tvOptions[i].setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ((HomeFragment) fragments.get(0)).initData();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    //抽屉菜单事件
    public boolean onNavigationItemSelected(MenuItem item) {

        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        return super.onContextItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            System.exit(0);
            return;
        } else {
            Toast.makeText(getBaseContext(), "再按一次返回退出程序", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }
}

