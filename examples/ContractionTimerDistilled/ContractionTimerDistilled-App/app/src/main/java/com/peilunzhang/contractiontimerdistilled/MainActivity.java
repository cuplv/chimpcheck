package com.peilunzhang.contractiontimerdistilled;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    FragmentManager fragmentManager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button firstFragmentBtn = (Button) findViewById(R.id.fragmentBtn1);
        Button secondFragmentBtn = (Button) findViewById(R.id.fragmentBtn2);

        firstFragmentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){
                fragmentManager.beginTransaction().
                        replace(R.id.fragment_container, new CountdownFragment()).commit();
            };
        });
        secondFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                fragmentManager.beginTransaction().
                        replace(R.id.fragment_container, new EmptyFragment()).commit();
            };
        });

        // String[] perms = { "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };
        // requestPermissions(perms,200);

    }
}
