package edu.colorado.plv.chimp.viewactions;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;

/**
 * Created by edmundlam on 5/16/17.
 */

public class ChimpActionFactory {


    private static CoordinatesProvider getCoordinatesProvider(final int x, final int y){
        return new CoordinatesProvider(){
            @Override
            public float[] calculateCoordinates(View view) {

                final int[] screenPos = new int[2];
                view.getLocationOnScreen(screenPos);

                final float screenX = screenPos[0] + x;
                final float screenY = screenPos[1] + y;

                return new float[]{screenX, screenY};
            }
        };
    }
    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                getCoordinatesProvider(x, y),
                Press.FINGER);
    }
    public static ViewAction longClickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.LONG,
                getCoordinatesProvider(x, y),
                Press.FINGER);
    }
}
