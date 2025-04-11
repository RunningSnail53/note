package edu.hebut.retrofittest.Util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import edu.hebut.retrofittest.Bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SPUtils {
    public static final String KEY_USER = "userKey";

    public static void insertUser(String username, String password, Context context) {
//        获取数据库写的权限 一般都是更新
        List<UserBean> userBeans = new Gson().fromJson(getData(KEY_USER, context), new TypeToken<List<UserBean>>() {
        }.getType());
        if (userBeans == null) {
            userBeans = new ArrayList<>();
        }
        userBeans.add(new UserBean(username, password));
        setData(KEY_USER, new Gson().toJson(userBeans),context);
    }

    //    查询数据库方法,  使用数据库读数据库权限的时候，不能调用sqLiteDatabase.close();
    public static UserBean query(String username, String password,Context context) {
        UserBean userBean = null;
        List<UserBean> userBeans = new Gson().fromJson(getData(KEY_USER, context), new TypeToken<List<UserBean>>() {
        }.getType());
        if (userBeans == null) {
            userBeans = new ArrayList<>();
        }
        for (int i = 0; i < userBeans.size(); i++) {
            if (userBeans.get(i).getUsername().equals(username) && userBeans.get(i).getPwd().equals(password)) {
                userBean = new UserBean(userBeans.get(i).getUsername(), userBeans.get(i).getPwd());
                return userBean;
            }
        }
        return userBean;
    }

    public static boolean updatePwd(String username, String oldPwd, String newPwd, Context context) {
        UserBean userBean = null;
        List<UserBean> userBeans = new Gson().fromJson(getData(KEY_USER, context), new TypeToken<List<UserBean>>() {
        }.getType());
        if (userBeans != null) {
            for (int i = 0; i < userBeans.size(); i++) {
                if (userBeans.get(i).getUsername().equals(username) && userBeans.get(i).getPwd().equals(oldPwd)) {
                   userBeans.get(i).setPwd(newPwd);
                    return true;
                }
            }
        }
        return false;
    }

    public static String getData(String key, Context context) {
        return context.getSharedPreferences("action", MODE_PRIVATE).getString(key, "");
    }

    public static void setData(String key, String action, Context context) {
        context.getSharedPreferences("action", MODE_PRIVATE).edit().putString(key, action).commit();
    }


}
