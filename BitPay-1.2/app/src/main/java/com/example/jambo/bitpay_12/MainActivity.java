package com.example.jambo.bitpay_12;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback,NfcAdapter.OnNdefPushCompleteCallback {
    NfcAdapter mAdapter,mNfcAdapter;
    TextView promt,textname;
    NdefMessage mNdefPushMessage;
    PendingIntent mPendingIntent;
    String[][] techListsArray;
    String datas;
    Button btntoBTC,btntoMyApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promt = (TextView) findViewById(R.id.tagid);
        textname = (TextView)findViewById(R.id.textname);
        gotoBTC(); //轉跳至BTC的凾式
        gotoMyApp(); //跳轉至自定義的Activity
        //Thread 建置
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        // 获取默认的NFC控制器
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            promt.setText("設備不支持NFC！");
            finish();
            return;
        }
        if (!mAdapter.isEnabled()) {
            promt.setText("请在系统設置中先啟用NFC功能！");
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        //AndroidBeam
        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()), 0);

        mNfcAdapter.setNdefPushMessageCallback(this, this);
       // mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public void onNewIntent(Intent paramIntent) {
        setIntent(paramIntent);
        resolveIntent(paramIntent);
        processIntent(paramIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mAdapter == null)
            return;
        if (!this.mAdapter.isEnabled()) {
            promt.setText("请在系统设置中先启用NFC功能！");
        }
        this.mAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, null);
    }


    protected void resolveIntent(Intent intent) {

        // 得到是否检测到TAG触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            // 处理该intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // 获取标签id数组
            byte[] bytesId = tag.getId();
            String str = new String(bytesId);
            //获取消息内容
            NfcMessageParser nfcMessageParser = new NfcMessageParser(intent);
            List<String> tagMessage = nfcMessageParser.getTagMessage();

            if (tagMessage == null || tagMessage.size() == 0) {

                //Toast.makeText(this, "NFC格式不支持...", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < tagMessage.size(); i++) {
                    Log.e("tag", tagMessage.get(i));
                }
                datas = tagMessage.get(0);
            }


            String info = bytesToHexString(bytesId) ;
            String[] techList = tag.getTechList();
            //分析NFC卡的类型： Mifare Classic/UltraLight Info
            String cardType = "";


            for (String aTechList : techList) {
                if (TextUtils.equals(aTechList, "android.nfc.tech.Ndef")) {
                    Ndef ndef = Ndef.get(tag);
                    cardType += "最大数据尺寸:" + ndef.getMaxSize() + "字节";
                }
            }
            promt.setText("商品tag id : " + info);
            getDB(info);
        }
    }

    private void getDB( String name) {
        try {
            String result = DBConnector.executeQuery(name);

                /*
                    SQL 結果有多筆資料時使用JSONArray
                    只有一筆資料時直接建立JSONObject物件
                    JSONObject jsonData = new JSONObject(result);
                */
            JSONArray jsonArray = new JSONArray(result);
            //if (jsonData.getString("name").equals(null))textname.setText("無商品資訊 !");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                if(jsonData.getString("name").equals(null))textname.setText("無商品資訊 !");
                 else textname.setText("商品名稱 : "+jsonData.getString("name") +"\n商品價格 : " +jsonData.getString("price")+" BTC");
            }

        } catch(Exception e) {
            Log.e("log_tag", e.toString());
        }
    }

    private void gotoBTC(){
        btntoBTC = (Button)findViewById(R.id.go_btc);
        btntoBTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "de.schildbach.wallet_test";
                PackageManager packageManager = getPackageManager();
                if (checkPackInfo(str)) {
                    Intent intent = packageManager.getLaunchIntentForPackage(str);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "尚未安裝 : "+str,  Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void gotoMyApp(){
        btntoMyApp = (Button)findViewById(R.id.go_MyApp);
        btntoMyApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //第一种方式
                ComponentName cn = new ComponentName("com.example.jambo.newactivity", "com.example.jambo.newactivity.Main2Activity");
                //ComponentName cn = new ComponentName("de.schildbach.wallet_tes", "641e8d7 de.schildbach.wallet.ui.send.SendCoinsActivity");
                try {
                    intent.setComponent(cn);
                    //第二种方式
                    //intent.setClassName("com.example.fm", "com.example.fm.MainFragmentActivity");
                    //intent.putExtra("test", "intent1");
                    startActivity(intent);
                } catch (Exception e) {
                    //TODO  可以在这里提示用户没有安装应用或找不到指定Activity，或者是做其他的操作
                }
            }
        });
    }



     // 检查包是否存在
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }




    /**
     * 数组转换成十六进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }






    //---返回鍵退出應用 確認
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(MainActivity.this)
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


    //androidBeam
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = textname.getText().toString().trim();
        if ("".equals(text))
            text = "默认文本";
        /*
         *  "com.android.calculator2"  官方原生计算器包。
         *  当另外一部手机靠近这部手机时，会启动计算器。
         *
         * */
        NdefMessage ndefMessage = new NdefMessage( new NdefRecord[] {
                NdefRecord .createApplicationRecord("com.example.jambo.androidbeam_2") }) ;

        //NdefMessage ndefMessage = new NdefMessage(
        //      new NdefRecord[] { createTextRecord(text) });

        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        Log.d("message", "complete");
    }
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String text = TextRecord.parse2(msg.getRecords()[0]).getText();
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }


}
