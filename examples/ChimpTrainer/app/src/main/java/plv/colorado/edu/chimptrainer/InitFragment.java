package plv.colorado.edu.chimptrainer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Pezh on 4/8/17.
 */

public class InitFragment extends Fragment {
    FragmentManager fragmentManager;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_begin, container, false);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        fragmentManager = this.getFragmentManager();
        Button buttonBegin = (Button) view.findViewById(R.id.button_begin);

        buttonBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();

            }
        });
    }

}
