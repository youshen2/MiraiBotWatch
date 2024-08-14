package top.moye.miraibotwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTool.setDefaultDisplay(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("server",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        EditText main_hosturl = (EditText) findViewById(R.id.main_hosturl);
        EditText main_hostport = (EditText) findViewById(R.id.main_hostport);
        EditText main_botqq = (EditText) findViewById(R.id.main_botqq);
        EditText main_session = (EditText) findViewById(R.id.main_session);
        main_hosturl.setText(sharedPreferences.getString("host_url",""));
        main_hostport.setText(String.valueOf(sharedPreferences.getInt("host_port",5508)));
        main_botqq.setText(String.valueOf(sharedPreferences.getInt("bot_qq",0)));
        main_session.setText(sharedPreferences.getString("session",""));
    }

    public void _on_server_ok_click(View view) {
        EditText main_hosturl = (EditText) findViewById(R.id.main_hosturl);
        EditText main_hostport = (EditText) findViewById(R.id.main_hostport);
        EditText main_botqq = (EditText) findViewById(R.id.main_botqq);
        EditText main_session = (EditText) findViewById(R.id.main_session);
        TextView main_error = (TextView) findViewById(R.id.main_error);
        if(main_hostport.getText().length() < 2){
            Toast.makeText(this,"端口太短",Toast.LENGTH_SHORT).show();
        }else if(main_hosturl.getText().length() < 4){
            Toast.makeText(this,"服务器地址太短",Toast.LENGTH_SHORT).show();
        }else if(main_session.getText().length() < 3){
            Toast.makeText(this,"Session不正确",Toast.LENGTH_SHORT).show();
        }else{
            editor.putString("host_url",main_hosturl.getText().toString());
            editor.putInt("host_port", Integer.parseInt(main_hostport.getText().toString()));
            editor.putInt("bot_qq", Integer.parseInt(main_botqq.getText().toString()));
            editor.putString("session",main_session.getText().toString());
            editor.commit();
            Intent intent = new Intent(MainActivity.this,AppActivity.class);
            startActivity(intent);
        }
    }
}