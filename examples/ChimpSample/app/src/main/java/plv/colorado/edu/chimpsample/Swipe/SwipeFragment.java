package plv.colorado.edu.chimpsample.Swipe;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import plv.colorado.edu.chimpsample.R;

/**
 * Created by Pezh on 4/10/17.
 */

public class SwipeFragment extends Fragment {
    SeekBar sbRed;
    SeekBar sbGreen;
    SeekBar sbBlue;
    View mDrawView;
    View view;

    int mDrawRed;
    int mDrawGreen;
    int mDrawBlue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_slider, container, false);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        sbRed = (SeekBar) view.findViewById(R.id.seekBar);
        sbGreen = (SeekBar) view.findViewById(R.id.seekBar2);
        sbBlue= (SeekBar) view.findViewById(R.id.seekBar3);


        mDrawView = view.findViewById(R.id.drawView);
        mDrawView.setBackgroundColor(Color.rgb(0, 0, 0));

        sbRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDrawRed = (i * 255 / 100);
                mDrawView.setBackgroundColor(Color.rgb(mDrawRed,mDrawGreen,mDrawBlue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                return;
            }
        });
        sbGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDrawGreen= (i * 255 / 100);
                mDrawView.setBackgroundColor(Color.rgb(mDrawRed,mDrawGreen,mDrawBlue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                return;
            }
        });
        sbBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDrawBlue= (i * 255 / 100);
                mDrawView.setBackgroundColor(Color.rgb(mDrawRed,mDrawGreen,mDrawBlue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                return;
            }
        });

    }

}
