package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String session = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_app);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        session = sharedPreferences.getString("session","");

        if (session.equals("")){
            Intent intent = new Intent(AppActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"Session不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_app_launcher);
        launcher.mCurrentScreen = 1;

        update_status();
        load_groups();
        load_friends();
    }

    void update_status(){
        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_app_launcher);
        ImageView launcher_status = (ImageView) findViewById(R.id.activity_app_status);
        switch (launcher.getCurrentScreen()){
            case 0:
                finish();
            case 1:
                launcher_status.setImageResource(R.drawable.frame_1in2);
                break;
            case 2:
                launcher_status.setImageResource(R.drawable.frame_2in2);
                break;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                update_status();
            }
        },300);
    }

    void load_groups(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/groupList?sessionKey=" + session)
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});

                    if((int)json_map.get("code") == 0){
                        Map<String, Object> finalJson_map1 = json_map;
                        AppActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_grouplist);
                                for (Map<String, Object> i : (ArrayList<Map>) finalJson_map1.get("data")){
                                    Button button = new Button(AppActivity.this);
                                    button.setEnabled(true);
                                    button.setAllCaps(false);
                                    button.setText((String)i.get("name"));
                                    button.setTop(10);
                                    button.setTextColor(getResources().getColor(R.color.font_title));
                                    button.setBackground(getResources().getDrawable(R.drawable.list_button));
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(AppActivity.this,GroupActivity.class);
                                            if(i.get("id") instanceof Integer) intent.putExtra("groupid",Long.valueOf((int)i.get("id")));
                                            else if(i.get("id") instanceof Long) intent.putExtra("groupid",(long)i.get("id"));
                                            intent.putExtra("groupname",(String)i.get("name"));
                                            startActivity(intent);
                                        }
                                    });
                                    layout.addView(button);
                                    TextView textView = new TextView(AppActivity.this);
                                    textView.setHeight(10);
                                    layout.addView(textView);
                                }
                            }
                        });
                    }else{
                        Map<String, Object> finalJson_map = json_map;
                        AppActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = new TextView(AppActivity.this);
                                textView.setText((String) finalJson_map.get("msg"));
                                textView.setTextColor(getResources().getColor(R.color.font_error));
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_grouplist);
                                layout.addView(textView);
                            }
                        });
                    }

                }catch (Exception e){
                    AppActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = new TextView(AppActivity.this);
                            textView.setText("发生错误：" + e.toString() + "\n请确认配置文件正确");
                            textView.setTextColor(getResources().getColor(R.color.font_error));
                            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_grouplist);
                            layout.addView(textView);
                        }
                    });
                }
                Looper.loop();
            }
        }).start();
    }

    void load_friends(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/friendList?sessionKey=" + session)
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});

                    if((int)json_map.get("code") == 0){
                        Map<String, Object> finalJson_map1 = json_map;
                        AppActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_friendlist);
                                for (Map<String, Object> i : (ArrayList<Map>) finalJson_map1.get("data")){
                                    Button button = new Button(AppActivity.this);
                                    button.setEnabled(true);
                                    button.setAllCaps(false);
                                    button.setText((String)i.get("nickname"));
                                    button.setTop(10);
                                    button.setTextColor(getResources().getColor(R.color.font_title));
                                    button.setBackground(getResources().getDrawable(R.drawable.list_button));
//                                    button.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            Intent intent = new Intent(AppActivity.this,FriendActivity.class);
//                                            intent.putExtra("qqid",(int)i.get("id"));
//                                            startActivity(intent);
//                                        }
//                                    });
                                    layout.addView(button);
                                    TextView textView = new TextView(AppActivity.this);
                                    textView.setHeight(10);
                                    layout.addView(textView);
                                }
                            }
                        });
                    }else{
                        Map<String, Object> finalJson_map = json_map;
                        AppActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = new TextView(AppActivity.this);
                                textView.setText((String) finalJson_map.get("msg"));
                                textView.setTextColor(getResources().getColor(R.color.font_error));
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_friendlist);
                                layout.addView(textView);
                            }
                        });
                    }

                }catch (Exception e){
                    AppActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = new TextView(AppActivity.this);
                            textView.setText("发生错误：" + e.toString() + "\n请确认配置文件正确");
                            textView.setTextColor(getResources().getColor(R.color.font_error));
                            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_app_friendlist);
                            layout.addView(textView);
                        }
                    });
                }
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
    }
}
