package edu.hebut.retrofittest.UI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

import edu.hebut.retrofittest.R;
import edu.hebut.retrofittest.Util.EditTextClearTools;
import edu.hebut.retrofittest.Util.PermissionsUtil;
import edu.hebut.retrofittest.Util.SharedDataUtils;
import edu.hebut.retrofittest.supabase.dao.UserDao;


public class LoginActivity extends AppCompatActivity {

    private EditText userName, passWord;
    private ImageView unameClear, pwdClear, pwdToggle;
    private TextView userReg;
    private android.widget.ImageView iv_icon;
    private Button login;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PermissionsUtil.checkAndRequestPermissions(this);
        init();//初始化组件
        loadSP();//读取SharedPreferences数据
        ViewClick();//注册组件点击事件
    }

    private void init() {
        userName = (EditText) findViewById(R.id.et_userName);
        passWord = (EditText) findViewById(R.id.et_password);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        pwdToggle = (ImageView) findViewById(R.id.iv_pwdToggle);
        iv_icon = (android.widget.ImageView) findViewById(R.id.iv_icon);
        userReg = (TextView) findViewById(R.id.link_signup);
        login = (Button) findViewById(R.id.btn_login);
        EditTextClearTools.addClearListener(userName, unameClear);
        EditTextClearTools.addClearListener(passWord, pwdClear);
        pwdToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });
    }

    private void loadSP() {
        sp = getSharedPreferences("userInfo", 0);
        ed = sp.edit();
        boolean remember_Pw = sp.getBoolean("REMEMBER_PW", false);
        boolean auto_Login = sp.getBoolean("AUTO_LOGIN", false);
        String names = sp.getString("USER_NAME", "");
        String passwords = sp.getString("PASSWORD", "");
        if (remember_Pw) {
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
    }

    private void login() {
        String username = userName.getText().toString().trim();
        String password = passWord.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(getApplicationContext(), "帐号不能为空", Toast.LENGTH_LONG).show();
            return;
        } else if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        runOnUiThread(() -> {
            // 1. 创建带超时控制的ProgressDialog
            ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("登录中...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // 设置10秒超时
            new Handler().postDelayed(() -> {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "连接超时，请检查网络", Toast.LENGTH_SHORT).show();
                }
            }, 10000);

            // 2. 发起登录请求
            UserDao.Companion.getUserByNameAndPassword(username, password).thenAccept(user -> {
                if (user != null) {
                    // 3. 使用线程池处理数据初始化
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {

                            SharedDataUtils.init(user).join();
                            runOnUiThread(() -> {
                                progressDialog.dismiss();

                                // 4. 添加跳转动画
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "数据初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        // 5. 改进错误提示
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("登录失败")
                                .setMessage("用户名或密码错误")
                                .setPositiveButton("重试", null)
                                .show();
                    });
                }
            }).exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    // 6. 网络异常处理
                    Toast.makeText(LoginActivity.this, "网络错误: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
                return null;
            });
        });

    }

    private void goReg() {
        userReg.setTextColor(Color.rgb(0, 0, 0));
        Intent intent = new Intent(getApplicationContext(), RegActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void ViewClick() {

        userReg.setOnClickListener(view -> goReg());

        login.setOnClickListener(view -> login());


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

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            // 显示密码
            passWord.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            pwdToggle.setImageResource(R.drawable.ic_visibility_on);
        } else {
            // 隐藏密码
            passWord.setTransformationMethod(PasswordTransformationMethod.getInstance());
            pwdToggle.setImageResource(R.drawable.ic_visibility_off);
        }

        // 保持光标位置
        int selection = passWord.getSelectionEnd();
        passWord.setText(passWord.getText()); // 解决字体重绘问题
        passWord.setSelection(Math.min(selection, passWord.getText().length()));

        // 无障碍支持
        pwdToggle.setContentDescription(isPasswordVisible ? "隐藏密码" : "显示密码");
    }

}
