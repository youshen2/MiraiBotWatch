package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class GroupNoticeActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String session = "";

    long senderId = 0;
    String content = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_groupnotice);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        session = sharedPreferences.getString("session","");

        Intent self_intent = getIntent();
        senderId = self_intent.getLongExtra("senderId",0);
        content = self_intent.getStringExtra("content");

        if (session.equals("")){
            Intent intent = new Intent(GroupNoticeActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(this,"Session不存在",Toast.LENGTH_SHORT).show();
            finish();
        }

        TextView content_text = (TextView) findViewById(R.id.activity_groupnotice_content);
        content_text.setText(content);
        TextView sender = (TextView) findViewById(R.id.activity_groupnotice_sender);
        sender.setText("—— " + String.valueOf(senderId));
    }
}
