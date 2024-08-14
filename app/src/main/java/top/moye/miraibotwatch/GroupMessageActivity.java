package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class GroupMessageActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String session = "";

    long groupid = 0;
    long memberid = 0;
    long messageid = 0;
    String content = "";
    String member_name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_groupmessage);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        session = sharedPreferences.getString("session","");

        Intent self_intent = getIntent();
        groupid = self_intent.getLongExtra("groupid",0);
        messageid = self_intent.getLongExtra("messageid",0);
        memberid = self_intent.getLongExtra("memberid",0);
        content = self_intent.getStringExtra("content");
        member_name = self_intent.getStringExtra("member_name");

        if (session.equals("")){
            Intent intent = new Intent(GroupMessageActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"Session不存在",Toast.LENGTH_SHORT).show();
            finish();
        }


        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupmessageinfo_launcher);
        launcher.mCurrentScreen = 1;

        TextView mem = (TextView) findViewById(R.id.activity_groupmessageinfo_member_name);
        mem.setText(member_name);
        TextView cont = (TextView) findViewById(R.id.activity_groupmessageinfo_content);
        cont.setText(content);
        update_status();
    }

    void update_status(){
        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupmessageinfo_launcher);
        ImageView launcher_status = (ImageView) findViewById(R.id.activity_groupmessageinfo_status);
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


    @Override
    public void onBackPressed() {
    }


    public void _on_msg_recall_click(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(groupid) + ",\"messageId\":" + String.valueOf(messageid) + "}";
                RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/recall")
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
                        Toast.makeText(GroupMessageActivity.this,"消息撤回成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(GroupMessageActivity.this,(String)json_map.get("msg"),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                }
                Looper.loop();
            }
        }).start();
    }

    public void open_member(View view) {
        Intent intent = new Intent(GroupMessageActivity.this,GroupMemberActivity.class);
        intent.putExtra("memberid",memberid);
        intent.putExtra("member_nickname",member_name);
        intent.putExtra("groupid",groupid);
        startActivity(intent);
    }
}
