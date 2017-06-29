package com.example.jambo.bitpay_12;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

public class LoginPage extends AppCompatActivity {
    Button btnLogin;
    EditText username, password;
    Thread Login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //將介面和程式掛勾
        username = (EditText) findViewById(R.id.Edtacc);
        password = (EditText) findViewById(R.id.edtpwd);
        //建立按鈕的OnClickListener
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //執行子程序
                Login = new Login();
                Login.start();
            }
        });
    }
    //執行連接網絡所需的子程序
    class Login extends Thread {
        @Override
        public void run() {
            //建立HttpClient用以跟伺服器溝通
            HttpClient client = new DefaultHttpClient();
            try {
                HttpPost post = new HttpPost("http://120.125.85.162/test/login.php");

                //建立POST的變數
                List<NameValuePair> vars = new ArrayList<NameValuePair>();
                vars.add(new BasicNameValuePair("Username", username.getText().toString()));
                vars.add(new BasicNameValuePair("Password", password.getText().toString()));

                //發出POST要求
                post.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

                //建立ResponseHandler,以接收伺服器回傳的訊息
                ResponseHandler<String> h = new BasicResponseHandler();

                //將回傳的訊息轉為String
                String response = new String(client.execute(post, h).getBytes(), HTTP.UTF_8);
                String success = "Login Succeeded";
                Looper.prepare();
                //若回傳的訊息等於"Login Succeeded"，跳轉到另一個頁面
                if (response.equals(success)) {
                    Intent i = new Intent(LoginPage.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                //否則只顯示回傳訊息
                else {
                    Toast.makeText(LoginPage.this, response, Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            } catch (Exception ex) {
                //若伺服器無法與PHP檔連接時的動作
            }
        }

    }


    //---返回鍵退出應用 確認
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(LoginPage.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub

                                }
                            }).show();
        }
        return true;
    }
}
