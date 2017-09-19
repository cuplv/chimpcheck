package plv.colorado.edu.clickingtestapp;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import chimp.protobuf.AppEventOuterClass;

public class MainActivity extends AppCompatActivity {

    Button btnClickTest;
    Button btnTypeTest;
    Button btnSwipeTest;

    Toast toastMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);




        btnClickTest = $(R.id.btnClickTest);
        btnClickTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itn = new Intent(MainActivity.this, ClickActivity.class);
                startActivity(itn);
            }
        });
        btnClickTest.setLongClickable(true);
        btnClickTest.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.callOnClick();
                return true;
            }
        });

        btnTypeTest = $(R.id.btnTypeTest);

        btnTypeTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itn = new Intent(MainActivity.this, TypeActivity.class);
                startActivity(itn);
            }
        });




    }

    private void showToast(String msg){
        if(toastMessage != null){
            toastMessage.cancel();
        }

        toastMessage = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
        toastMessage.show();
    }

    private <T> T $(@IdRes int resid) {
        return (T) findViewById(resid);
    }

}
