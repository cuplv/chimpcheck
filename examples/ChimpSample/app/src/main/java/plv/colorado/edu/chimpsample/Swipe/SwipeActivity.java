package plv.colorado.edu.chimpsample.Swipe;

import android.os.Bundle;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import plv.colorado.edu.chimpsample.Intermedia.IntermediaFragment;
import plv.colorado.edu.chimpsample.R;

/**
 * Created by Pezh on 4/10/17.
 */

public class SwipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeFragment swipeFragment = new SwipeFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, swipeFragment).commit();

    }


    @Override
    protected void onResume(){
        super.onResume();
        View root = this.getWindow().getDecorView();

        for(View v : TreeIterables.breadthFirstViewTraversal(root)){
            if(v.getId() != -1) {
                System.out.println("View: " + v.getResources().getResourceEntryName(v.getId()) + " " + v.isClickable());
            } else if(v.getContentDescription() != null){
                System.out.println("View: " + v.getContentDescription().toString() + " " + v.isClickable());
            } else{
                System.out.println("View: " + v.toString() + " No Des, No Id " + v.isClickable());
            }

        }
    }
}
