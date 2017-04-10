package plv.colorado.edu.chimpsample;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    @Override
    protected void onResume(){
        super.onResume();
        fragmentManager = getFragmentManager();
        InitFragment initFragment = new InitFragment();
        fragmentManager.beginTransaction().replace(R.id.main_fragment_container, initFragment).commit();

    }





}
