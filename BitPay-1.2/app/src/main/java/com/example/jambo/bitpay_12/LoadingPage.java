package com.example.jambo.bitpay_12;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class LoadingPage extends AppCompatActivity {
    ImageView iv;
    ProgressBar pgbar;
    public SharedPreferences UserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_page);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全螢幕
        iv = (ImageView)findViewById(R.id.imageView3) ;
        //pgbar = (ProgressBar)findViewById(R.id.pgbar) ;


        alpha(iv,2000);
        progressrun();
        mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 2000); //2秒跳轉
    }
    private static final int GOTO_MAIN_ACTIVITY = 0;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent();
            //將原本Activity的換成MainActivity
            intent.setClass(LoadingPage.this, LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    };
    //透明度動畫
    public void alpha(ImageView imageView,int time){
        Animation alphaAnimation=new AlphaAnimation(0.3f,1.0f);
        alphaAnimation.setDuration(time);                  //设置持续时间
        imageView.setAnimation(alphaAnimation);             //设置动画
        alphaAnimation.startNow();
    }
    //進度條執行
    public void progressrun(){
        new Thread(){
            public void run(){
                try {
                    for(int i=0;i<6;i++) {
                        pgbar.incrementProgressBy(10);
                        sleep(1000);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

}