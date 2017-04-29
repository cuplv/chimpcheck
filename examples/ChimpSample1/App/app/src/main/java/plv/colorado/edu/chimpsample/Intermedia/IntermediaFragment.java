package plv.colorado.edu.chimpsample.Intermedia;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import plv.colorado.edu.chimpsample.Countdown.CountdownActivity;
import plv.colorado.edu.chimpsample.R;
import plv.colorado.edu.chimpsample.Swipe.SwipeActivity;

/**
 * Created by Pezh on 4/10/17.
 */

public class IntermediaFragment extends Fragment {

    View view;
    Button btnCdt;
    Button btnSwipe;
    Button btnBack;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_intermedia, container, false);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        fragmentManager = this.getFragmentManager();
        btnCdt = (Button) view.findViewById(R.id.interm_btn_cdt);
        btnSwipe= (Button) view.findViewById(R.id.interm_btn_swipe);
        btnBack= (Button) view.findViewById(R.id.interm_btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        btnSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), SwipeActivity.class);
                startActivity(intent);
            }
        });
        btnCdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), CountdownActivity.class);
                startActivity(intent);
            }
        });
    }


}
