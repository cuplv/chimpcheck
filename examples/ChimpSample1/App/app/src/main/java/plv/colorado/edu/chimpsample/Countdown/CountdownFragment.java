package plv.colorado.edu.chimpsample.Countdown;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import plv.colorado.edu.chimpsample.R;

/**
 * Created by Pezh on 4/9/17.
 */

public class CountdownFragment extends Fragment {

    View view;
    Button btnCount5;
    Button btnCount10;
    Button btnBack;
    TextView textCount;
    CountDownTimer cdt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_countdown, container, false);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        textCount = (TextView) view.findViewById(R.id.tv_countdown);
        btnCount5 = (Button) view.findViewById(R.id.btn_count10);
        btnCount10 = (Button) view.findViewById(R.id.btn_count30);
        btnBack = (Button) view.findViewById(R.id.btn_back);


        textCount.setText(R.string.count_default);

        btnCount5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCounting(5000);

            }
        });
        btnCount10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCounting(10000);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }


    public void startCounting(long cdtTime){
        if(cdt != null){
            cdt.cancel();
        }
        cdt = new CountDownTimer(cdtTime, 1000) {
            @Override
            public void onTick(long l) {
                textCount.setText("seconds remaining: " + l / 1000);
            }

            @Override
            public void onFinish() {
                textCount.setText(getString(R.string.count_default));
            }
        }.start();
    }
}
