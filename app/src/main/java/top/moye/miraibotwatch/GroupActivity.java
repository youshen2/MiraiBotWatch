package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String session = "";
    long group_id = 0;
    String group_name = "未命名";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_groupinfo);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        session = sharedPreferences.getString("session","");

        Intent self_intent = getIntent();
        group_id = self_intent.getLongExtra("groupid",0);
        group_name = self_intent.getStringExtra("groupname");

        if (session.equals("")){
            Intent intent = new Intent(GroupActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"Session不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        if (group_id == 0){
            Toast.makeText(this,"群ID不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupinfo_launcher);
        launcher.mCurrentScreen = 1;

        TextView group_name_textview = (TextView) findViewById(R.id.activity_groupinfo_groupname);
        group_name_textview.setText(group_name);
        TextView group_id_textview = (TextView) findViewById(R.id.activity_groupinfo_groupid);
        group_id_textview.setText(String.valueOf(group_id));
        TextView group_member_count = (TextView) findViewById(R.id.activity_groupinfo_membercount);
        group_member_count.setText("请等待加载完成，在此之前不要滑动");

        update_status();
        load_group_info();
        load_announcements();
        load_messages();
    }

    void update_status(){
        DragableLauncher launcher = (DragableLauncher) findViewById(R.id.activity_groupinfo_launcher);
        ImageView launcher_status = (ImageView) findViewById(R.id.activity_groupinfo_status);
        switch (launcher.getCurrentScreen()){
            case 0:
                finish();
            case 1:
                launcher_status.setImageResource(R.drawable.frame_1in4);
                break;
            case 2:
                launcher_status.setImageResource(R.drawable.frame_2in4);
                break;
            case 3:
                launcher_status.setImageResource(R.drawable.frame_3in4);
                break;
            case 4:
                launcher_status.setImageResource(R.drawable.frame_4in4);
                break;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                update_status();
            }
        },300);
    }

    void load_group_info(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/groupConfig?sessionKey=" + session + "&target=" + String.valueOf(group_id))
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});


                    Map<String, Object> finalJson_map = json_map;
                    GroupActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToggleButton group_muteall_textview = (ToggleButton) findViewById(R.id.activity_groupinfo_mute_all);
                            group_muteall_textview.setChecked((boolean) finalJson_map.get("muteAll"));
                            group_muteall_textview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    _on_muteall_click(b);
                                }
                            });
                        }
                    });

                }catch (Exception e){
                    System.out.println(e);
                    Toast.makeText(GroupActivity.this,"发生错误：" + e.toString(),Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/memberList?sessionKey=" + session + "&target=" + String.valueOf(group_id))
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});


                    Map<String, Object> finalJson_map = json_map;
                    GroupActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView group_member_count = (TextView) findViewById(R.id.activity_groupinfo_membercount);
                            group_member_count.setText("群人数：" + String.valueOf(((ArrayList<Map>) finalJson_map.get("data")).size()) + "人");

                            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_groupinfo_memberlist);
                            for (Map<String, Object> i : (ArrayList<Map>) finalJson_map.get("data")){
                                Button button = new Button(GroupActivity.this);
                                button.setEnabled(true);
                                button.setAllCaps(false);
                                if(Objects.equals((String) i.get("permission"), "OWNER"))  button.setText((String)i.get("memberName") + "\n群主");
                                else if(Objects.equals((String) i.get("permission"), "ADMINISTRATOR"))  button.setText("\n" + (String)i.get("memberName") + "\n管理员");
                                else if(Objects.equals((String) i.get("permission"), "MEMBER"))  button.setText((String)i.get("memberName") + "\n成员");
                                button.setTextColor(getResources().getColor(R.color.font_title));
                                button.setBackground(getResources().getDrawable(R.drawable.list_button));
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(GroupActivity.this,GroupMemberActivity.class);
                                        if(i.get("id") instanceof Integer) intent.putExtra("memberid",Long.valueOf((int)i.get("id")));
                                        else if(i.get("id") instanceof Long) intent.putExtra("memberid",(long)i.get("id"));
                                        intent.putExtra("member_nickname",(String)i.get("memberName"));
                                        intent.putExtra("groupid",group_id);
                                        startActivity(intent);
                                    }
                                });
                                layout.addView(button);
                                TextView textView = new TextView(GroupActivity.this);
                                textView.setHeight(10);
                                layout.addView(textView);
                            }
                        }
                    });

                }catch (Exception e){
                    System.out.println(e);
                    Toast.makeText(GroupActivity.this,"发生错误：" + e.toString(),Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }).start();
    }

    void load_announcements(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/anno/list?sessionKey=" + session + "&id=" + String.valueOf(group_id))
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});


                    if((int)json_map.get("code") == 0){
                        Map<String, Object> finalJson_map = json_map;
                        GroupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_groupinfo_announcement);
                                for (Map<String, Object> i : (ArrayList<Map>) finalJson_map.get("data")){
                                    Button button = new Button(GroupActivity.this);
                                    button.setEnabled(true);
                                    button.setAllCaps(false);
                                    Date date = new Date((int)i.get("publicationTime"));
                                    button.setText(date.toString());
                                    button.setTextColor(getResources().getColor(R.color.font_title));
                                    button.setBackground(getResources().getDrawable(R.drawable.list_button));
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(GroupActivity.this,GroupNoticeActivity.class);
                                            if(i.get("senderId") instanceof Integer) intent.putExtra("senderId",Long.valueOf((int)i.get("senderId")));
                                            else if(i.get("senderId") instanceof Long) intent.putExtra("senderId",(long)i.get("senderId"));
                                            intent.putExtra("content",(String)i.get("content"));
                                            startActivity(intent);
                                        }
                                    });
                                    layout.addView(button);
                                    TextView textView = new TextView(GroupActivity.this);
                                    textView.setHeight(10);
                                    layout.addView(textView);
                                }
                            }
                        });
                    }else{
                        Map<String, Object> finalJson_map1 = json_map;
                        GroupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = new TextView(GroupActivity.this);
                                textView.setText((String) finalJson_map1.get("msg"));
                                textView.setTextColor(getResources().getColor(R.color.font_error));
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_groupinfo_announcement);
                                layout.addView(textView);
                            }
                        });
                    }

                }catch (Exception e){
                    System.out.println(e);
                    Toast.makeText(GroupActivity.this,"发生错误：" + e.toString(),Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }).start();
    }

    void load_messages(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Request request = new Request.Builder()
                        .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/peekLatestMessage?sessionKey=" + session + "&count=30")
                        .build();
                Call call = new OkHttpClient().newCall(request);
                Response response = null;
                try {
                    response = call.execute();

                    Map<String, Object> json_map = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    json_map = mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});


                    if((int)json_map.get("code") == 0){
                        Map<String, Object> finalJson_map = json_map;
                        GroupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_groupinfo_messagelist);
                                for (Map<String, Object> i : (ArrayList<Map>) finalJson_map.get("data")){
                                    if(Objects.equals((String) i.get("type"), "GroupMessage")){
                                        if(Long.valueOf((int) ((Map<String,Object>)((Map<String,Object>)i.get("sender")).get("group")).get("id")) == group_id){
                                            Button button = new Button(GroupActivity.this);
                                            button.setEnabled(true);
                                            button.setAllCaps(false);
                                            String msg_text = "";
                                            int index1 = 0;
                                            for(Map<String, Object> j : (ArrayList<Map>) i.get("messageChain")){
                                                index1++;
                                                if(index1 == 1) continue;
                                                else if(Objects.equals((String) j.get("type"), "Plain")){
                                                    msg_text += (String)j.get("text");
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Image")){
                                                    msg_text += "[图片]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Forward")){
                                                    msg_text += "[聊天记录]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "File")){
                                                    msg_text += "[文件]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "App")){
                                                    msg_text += "[应用]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Xml")){
                                                    msg_text += "[XML]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Json")){
                                                    msg_text += "[JSON]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Voice")){
                                                    msg_text += "[语音]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "FlashImage")){
                                                    msg_text += "[闪照]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "Face")){
                                                    msg_text += "[表情]";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "At")){
                                                    msg_text += (String) j.get("display") + " ";
                                                }
                                                else if(Objects.equals((String) j.get("type"), "AtAll")){
                                                    msg_text += "@全体成员 ";
                                                }
                                            }
                                            Date date = new Date((int)((Map<String,Object>)((ArrayList<Map>) i.get("messageChain")).get(0)).get("time"));
                                            button.setText((String) ((Map<String,Object>)i.get("sender")).get("memberName") + "\n" + date.toString());
                                            button.setTextColor(getResources().getColor(R.color.font_title));
                                            button.setBackground(getResources().getDrawable(R.drawable.list_button));
                                            String finalMsg_text = msg_text;
                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent = new Intent(GroupActivity.this,GroupMessageActivity.class);
                                                    if(((Map<String,Object>)((ArrayList<Map>) i.get("messageChain")).get(0)).get("id") instanceof Integer) intent.putExtra("messageid",Long.valueOf((int)((Map<String,Object>)((ArrayList<Map>) i.get("messageChain")).get(0)).get("id")));
                                                    else if(((Map<String,Object>)((ArrayList<Map>) i.get("messageChain")).get(0)).get("id") instanceof Long) intent.putExtra("messageid",(long)((Map<String,Object>)((ArrayList<Map>) i.get("messageChain")).get(0)).get("id"));
                                                    if(((Map<String,Object>)i.get("sender")).get("id") instanceof Integer) intent.putExtra("memberid",Long.valueOf((int)((Map<String,Object>)i.get("sender")).get("id")));
                                                    else if(((Map<String,Object>)i.get("sender")).get("id") instanceof Long) intent.putExtra("memberid",(long)((Map<String,Object>)i.get("sender")).get("id"));
                                                    intent.putExtra("groupid",group_id);
                                                    intent.putExtra("member_name", (String) ((Map<String,Object>)i.get("sender")).get("memberName"));
                                                    intent.putExtra("content", finalMsg_text);
                                                    startActivity(intent);
                                                }
                                            });
                                            layout.addView(button);
                                            TextView textView = new TextView(GroupActivity.this);
                                            textView.setHeight(10);
                                            layout.addView(textView);
                                        }else{
                                            continue;
                                        }
                                    }else{
                                        continue;
                                    }
                                }
                            }
                        });
                    }else{
                        Map<String, Object> finalJson_map1 = json_map;
                        GroupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = new TextView(GroupActivity.this);
                                textView.setText((String) finalJson_map1.get("msg"));
                                textView.setTextColor(getResources().getColor(R.color.font_error));
                                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_groupinfo_announcement);
                                layout.addView(textView);
                            }
                        });
                    }

                }catch (Exception e){
                    System.out.println(e);
                    Toast.makeText(GroupActivity.this,"发生错误：" + e.toString(),Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
    }

    public void _on_muteall_click(Boolean a) {
        if(a){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    String json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(group_id) + "}";
                    RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                    Request request = new Request.Builder()
                            .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/muteAll")
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
                            Toast.makeText(GroupActivity.this,"全体禁言已开启",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(GroupActivity.this,(String)json_map.get("msg"),Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                    }
                    Looper.loop();
                }
            }).start();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    String json = "{\"sessionKey\":\"" + session + "\",\"target\":" + String.valueOf(group_id) + "}";
                    RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), json);
                    Request request = new Request.Builder()
                            .url("http://" + sharedPreferences.getString("host_url", "") + ":" + String.valueOf(sharedPreferences.getInt("host_port", 0)) + "/unmuteAll")
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
                            Toast.makeText(GroupActivity.this,"全体禁言已关闭",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(GroupActivity.this,(String)json_map.get("msg"),Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                    }
                    Looper.loop();
                }
            }).start();
        }
    }

}
