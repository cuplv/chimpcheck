package plv.colorado.edu.chimpsample.Intermedia;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import plv.colorado.edu.chimpsample.R;

/**
 * Created by Pezh on 4/10/17.
 */

public class IntermediaActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntermediaFragment interFragment = new IntermediaFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, interFragment).commit();

    }
}
