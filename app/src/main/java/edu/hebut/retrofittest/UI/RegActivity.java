package edu.hebut.retrofittest.UI;

import android.content.Intent;
import android.graphics.Color;
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
import edu.hebut.retrofittest.Util.SPUtils;
import edu.hebut.retrofittest.supabase.entity.User;

public class RegActivity extends AppCompatActivity {
    private EditText userName,passWord,rePassword;
    private ImageView unameClear,pwdClear,repwdClear;
    private TextView userLogin;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        init();
        ViewClick();
    }

    private void ViewClick() {

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin.setTextColor(Color.rgb(0, 0, 0));
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username=userName.getText().toString();
                final String password=passWord.getText().toString();
                final String repassword=rePassword.getText().toString();
                if(username.isEmpty()){
                    Toast.makeText(getApplicationContext(),"帐号不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }else if(!password.equals(repassword)){
                    Toast.makeText(getApplicationContext(),"两次密码输入不一致",Toast.LENGTH_LONG).show();
                    return;
                }

                UserBean userBean = SPUtils.query(username.trim(), password.trim(),RegActivity.this);
                if (userBean!=null) {
                    Toast.makeText(getApplicationContext(),"该用户已被注册，请重新输入",Toast.LENGTH_LONG).show();
                    userName.requestFocus();
                }else{
                    User user = new User(0 ,username, password);

                    SPUtils.insertUser(username,password,RegActivity.this);
                    Toast.makeText(getApplicationContext(),"用户注册成功，请前往登录",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
                }

            }
        });
    }

    private void init() {
        userName = (EditText) findViewById(R.id.et_userName);
        passWord = (EditText) findViewById(R.id.et_password);
        rePassword = (EditText) findViewById(R.id.et_repassword);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        repwdClear = (ImageView) findViewById(R.id.iv_repwdClear);
        userLogin = (TextView) findViewById(R.id.link_signup);
        register= (Button) findViewById(R.id.btn_login);
        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(passWord,pwdClear);
        EditTextClearTools.addClearListener(rePassword,repwdClear);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_out, R.anim.push_right_in);
    }
}
