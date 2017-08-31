package plv.colorado.edu.clickingtestapp;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Pezh on 8/23/17.
 */

public class ClickActivity extends AppCompatActivity{
    ListView mListView;
    Button btnActivated;
    Button btnClickable;
    Button btnEnable;

    Toast toastMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);


        mListView = $(R.id.main_list_view);
        final ArrayList<String> wordList = new ArrayList<>(Arrays.asList("Apple", "Banana", "Peach", "Dragon Fruit", "Strawberry"));
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.simple_list_item, wordList);
        mListView.setAdapter(adapter);

        btnActivated = $(R.id.button1);
        btnClickable = $(R.id.button2);
        btnEnable = $(R.id.button3);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showToast("Click on: " + wordList.get(i));
            }
        });
        btnActivated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setActivated(false);
                showToast("BTN: Activated");
            }
        });
        btnClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setClickable(false);
                showToast("BTN: Clickable");
            }
        });
        btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                showToast("BTN: Enable");
            }
        });



    }

    private void showToast(String msg){
        if(toastMessage != null){
            toastMessage.cancel();
        }

        toastMessage = Toast.makeText(ClickActivity.this, msg, Toast.LENGTH_SHORT);
        toastMessage.show();
    }

    private <T> T $(@IdRes int resid) {
        return (T) findViewById(resid);
    }
}
