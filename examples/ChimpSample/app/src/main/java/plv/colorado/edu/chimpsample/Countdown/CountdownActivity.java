package plv.colorado.edu.chimpsample.Countdown;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import plv.colorado.edu.chimpsample.R;

/**
 * Created by Pezh on 4/8/17.
 */

public class CountdownActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CountdownFragment cdtFragment = new CountdownFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, cdtFragment).commit();

    }

}
