package com.peilunzhang.contractiontimerdistilled;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    int count = 0;

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
                count++;
                fragmentManager.beginTransaction().
                        replace(R.id.fragment_container, new CountdownFragment()).commit();
            };
        });
        secondFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                count++;
                fragmentManager.beginTransaction().
                        replace(R.id.fragment_container, new EmptyFragment()).commit();
            };
        });

        Log.i("ContractionTimer-App","On Create: count is now " + count);

        // String[] perms = { "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };
        // requestPermissions(perms,200);

    }

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        Log.i("ContractionTimer-App","On Resume: count is now " + count);
    }

    public int getCount() { return count; }
}
