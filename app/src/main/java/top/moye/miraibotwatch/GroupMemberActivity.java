package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupMemberActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String session = "";

    long memberid = 0;
    long groupid = 0;
    String member_nickname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_groupmemberinfo);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        session = sharedPreferences.getString("session","");

        Intent self_intent = getIntent();
        memberid = self_intent.getLongExtra("memberid",0);
        groupid = self_intent.getLongExtra("groupid",0);
        member_nickname = self_intent.getStringExtra("member_nickname");

        if (session.equals("")){
            Intent intent = new Intent(GroupMemberActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"Session不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        if (memberid == 0){
            Toast.makeText(this,"群成员不存在",Toast.LENGTH_SHORT).show();
            finish();
        }
        if (groupid == 0){
            Toast.makeText(this,"群聊不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupmemberinfo_launcher);
        launcher.mCurrentScreen = 1;

        TextView nick_name = (TextView) findViewById(R.id.activity_groupmemberinfo_nickname);
        nick_name.setText(member_nickname);
        TextView qqid = (TextView) findViewById(R.id.activity_groupmemberinfo_qqid);
        qqid.setText(String.valueOf(memberid));
        TextView gid = (TextView) findViewById(R.id.activity_groupmemberinfo_groupid);
        gid.setText("在" + String.valueOf(groupid) + "之中");

        update_status();
    }


    void update_status(){
        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupmemberinfo_launcher);
        ImageView launcher_status = (ImageView) findViewById(R.id.activity_groupmemberinfo_status);
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

    public void _on_gm_mute_click(View view) {
        EditText mute_time = (EditText) findViewById(R.id.activity_groupmemberinfo_mute_time);
        if(mute_time.getText().length() < 1){
            Toast.makeText(this,"禁言时间太短",Toast.LENGTH_SHORT).show();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    String json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(groupid) + ",\"memberId\":" + String.valueOf(memberid) + ",\"time\":" + String.valueOf((long)Long.parseLong(mute_time.getText().toString()) * 60) + "}";
                    RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                    Request request = new Request.Builder()
                            .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/mute")
                            .post(formBody)
                            .build();
                    Call call = new OkHttpClient().newCall(request);
                    Response response = null;
                    try {
                        response = call.execute();
                        Map<String, Object> json_map = new HashMap<>();
                        ObjectMapper mapper = new ObjectMapper();
                        json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});
                        if((int)json_map.get("code") == 0){
                            Toast.makeText(GroupMemberActivity.this,"禁言成功",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(GroupMemberActivity.this,(String)json_map.get("msg"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(GroupMemberActivity.this,"发生错误",Toast.LENGTH_SHORT).show();
                    }
                    Looper.loop();
                }
            }).start();
        }
    }

    public void _on_gm_unmute_click(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(groupid) + ",\"memberId\":" + String.valueOf(memberid) + "}";
                RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/unmute")
                        .post(formBody)
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});
                    if((int)json_map.get("code") == 0){
                        Toast.makeText(GroupMemberActivity.this,"取消禁言成功",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(GroupMemberActivity.this,(String)json_map.get("msg"),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(GroupMemberActivity.this,"发生错误",Toast.LENGTH_SHORT).show();
                }
                Looper.loop();
            }
        }).start();
    }


    private int kick_count = 0;
    public void _on_gm_kick_click(View view) {
        if(kick_count < 4){
            kick_count+=1;
            GroupMemberActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button kick_btn = (Button) findViewById(R.id.activity_groupmemberinfo_kick_btn);
                    kick_btn.setText("踢出(再点" + String.valueOf(5 - kick_count) + "下)");
                }
            });
        }else{
            kick_count = 0;
            GroupMemberActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button kick_btn = (Button) findViewById(R.id.activity_groupmemberinfo_kick_btn);
                    kick_btn.setText("踢出");
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    ToggleButton toggleButton = (ToggleButton) findViewById(R.id.activity_groupmemberinfo_kick_toggle);
                    String json;
                    if(toggleButton.isChecked()) json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(groupid) + ",\"memberId\":" + String.valueOf(memberid) + ",\"block\":true,\"msg\":\"您已被踢出本群\"}";
                    else json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(groupid) + ",\"memberId\":" + String.valueOf(memberid) + ",\"block\":false,\"msg\":\"您已被踢出本群\"}";
                    RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                    Request request = new Request.Builder()
                            .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/kick")
                            .post(formBody)
                            .build();
                    Call call = new OkHttpClient().newCall(request);
                    Response response = null;
                    try {
                        response = call.execute();

                        Map<String, Object> json_map = new HashMap<>();
                        ObjectMapper mapper = new ObjectMapper();
                        json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});
                        if((int)json_map.get("code") == 0){
                            Toast.makeText(GroupMemberActivity.this,"成功踢出此人",Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(GroupMemberActivity.this,(String)json_map.get("msg"),Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(GroupMemberActivity.this,"发生错误",Toast.LENGTH_SHORT).show();
                    }
                    Looper.loop();
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
    }
}
