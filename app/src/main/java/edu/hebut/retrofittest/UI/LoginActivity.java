package edu.hebut.retrofittest.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hebut.retrofittest.Bean.UserBean;
import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.EditTextClearTools;
import edu.hebut.retrofittest.Util.PermissionsUtil;
import edu.hebut.retrofittest.Util.SPUtils;

import de.hdodenhof.circleimageview.CircleImageView;


public class LoginActivity extends AppCompatActivity {

    private EditText userName,passWord;
    private ImageView unameClear,pwdClear;
    private TextView userReg;
    private android.widget.ImageView iv_icon;
    private Button login;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PermissionsUtil.checkAndRequestPermissions(this);
        init();//初始化组件
        loadSP();//读取SharedPreferences数据
        ViewClick();//注册组件点击事件
    }
    private void init(){
        userName = (EditText) findViewById(R.id.et_userName);
        passWord = (EditText) findViewById(R.id.et_password);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        iv_icon= (android.widget.ImageView) findViewById(R.id.iv_icon);
        userReg = (TextView) findViewById(R.id.link_signup);
        login= (Button) findViewById(R.id.btn_login);
        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(passWord,pwdClear);

    }

    private void loadSP() {
        sp = getSharedPreferences("userInfo", 0);
        ed = sp.edit();
        boolean remember_Pw =sp.getBoolean("REMEMBER_PW", false);
        boolean auto_Login =sp.getBoolean("AUTO_LOGIN", false);
        String names=sp.getString("USER_NAME","");
        String passwords=sp.getString("PASSWORD", "");
        if(remember_Pw){
            userName.setText(names);
            passWord.setText(passwords);
            Bitmap bt = BitmapFactory.decodeFile("/sdcard/Memo/" + names + "head.jpg");// 从SD卡中找头像，转换成Bitmap
            if (bt != null) {
                @SuppressWarnings("deprecation")
                Drawable drawable = new BitmapDrawable(bt);// 转换成drawable
                iv_icon.setImageDrawable(drawable);
            } else {
                //如果SD里面没有就使用默认头像
            }
        }
//        Intent intent=getIntent();
//        if(intent.getStringExtra("code")!=null){
//            if(intent.getStringExtra("code").equals("relogin")){
//                auto_Login=false;
//            }
//        }
//
//        if(auto_Login){
//            login();
//        }
    }

    private void login() {
        final String username=userName.getText().toString();
        final String password=passWord.getText().toString();

        if(username.isEmpty()){
            Toast.makeText(getApplicationContext(),"帐号不能为空",Toast.LENGTH_LONG).show();
            return;
        }else if(password.isEmpty()){
            Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_LONG).show();
            return;
        }

        UserBean userBean = SPUtils.query(username.trim(), password.trim(),LoginActivity.this);
        if (userBean!=null) {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this,MainActivity.class);
            intent.putExtra("login_user",username);
            getSharedPreferences("login",MODE_PRIVATE).edit().putBoolean("login",true).commit();
            getSharedPreferences("login",MODE_PRIVATE).edit().putString("login_user",username).commit();
            startActivity(intent);
            finish();
        }else{

            Toast.makeText(LoginActivity.this, "密码验证失败，请重新验证登录", Toast.LENGTH_SHORT).show();
        }

    }

    private void goReg() {
        userReg.setTextColor(Color.rgb(0, 0, 0));
        Intent intent = new Intent(getApplicationContext(), RegActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void ViewClick() {

        userReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goReg();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });


    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            System.exit(0);
            return;
        }
        else { Toast.makeText(getBaseContext(), "再按一次返回退出程序", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

}
