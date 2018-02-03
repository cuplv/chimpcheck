package edu.colorado.plv.chimp.managers;

import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.viewmatchers.ChildAtPosition;

import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by edmund on 5/2/17.
 */
public class ViewManager {

    public static enum ViewTargetType {
       Matcher, XYCoord, WildCard
    }

    public abstract class ViewTarget {
        public ViewTargetType targetType = null;

        public ViewTarget(ViewTargetType type) {
            targetType = type;
        }
    }

    public class ViewMatcherTarget extends ViewTarget {

        public Matcher<View> matcher;

        public ViewMatcherTarget(Matcher<View> matcher) {
            super(ViewTargetType.Matcher);
            this.matcher = matcher;
        }

    }

    public class ViewWildCardTarget extends ViewTarget {
        public ViewWildCardTarget() {
            super(ViewTargetType.WildCard);
        }
    }

    public class ViewXYTarget extends ViewTarget {
        public int x,y;

        public ViewXYTarget(int x, int y) {
            super(ViewTargetType.XYCoord);
            this.x = x;
            this.y = y;
        }
    }

    public ViewMatcherTarget mkMatcherTarget(Matcher<View> matcher) {
        return new ViewMatcherTarget(matcher);
    }

    public ViewWildCardTarget mkWildCardTarget() {
        return new ViewWildCardTarget();
    }

    public ViewXYTarget mkXYTarget(int x, int y) {
        return new ViewXYTarget(x, y);
    }


    public boolean hasWildCard(AppEventOuterClass.UIID uiid) {
        switch(uiid.getIdType()) {
            case R_ID: return false;
            case NAME_ID: return false;
            case XY_ID: return false;
            case ONCHILD_ID: return false;
            case TOP_ID: return false;
            case WILD_CARD: return true;
            case CONJUNCT_ID:
                return hasWildCard( uiid.getUiid1() ) || hasWildCard( uiid.getUiid2() );
            default:
                return false;
        }
    }

    public ArrayList<ViewTarget> retrieveTargets(AppEventOuterClass.UIID uiid) {

        ArrayList<ViewTarget> targets = new ArrayList<>();

        if (hasWildCard(uiid)) {
            targets.add( mkWildCardTarget() );
            return targets;
        }

        switch(uiid.getIdType()) {
            case R_ID:
                targets.add( mkMatcherTarget( allOf(withId(uiid.getRid())) ) );
                break;
            case NAME_ID:
                targets.add( mkMatcherTarget( allOf(withText(uiid.getNameid())) ) );
                targets.add( mkMatcherTarget( allOf(withContentDescription(uiid.getNameid())) ) );
                break;
            case XY_ID:
                targets.add( mkXYTarget( uiid.getXyid().getX(), uiid.getXyid().getY() ) );
                break;
            case ONCHILD_ID:
                AppEventOuterClass.UIID parent = uiid.getParentId();
                int childIdx = uiid.getChildIdx().getInt();
                switch(parent.getIdType()) {
                    case R_ID:
                        targets.add( mkMatcherTarget(new ChildAtPosition(withId(uiid.getParentId().getRid()), childIdx)) );
                        break;
                    case NAME_ID:
                        targets.add( mkMatcherTarget(new ChildAtPosition(withText(uiid.getNameid()) , childIdx)) );
                        targets.add( mkMatcherTarget(new ChildAtPosition(withContentDescription(uiid.getNameid()) , childIdx)) );
                        break;
                    default:
                        Log.e("ChimpDriver-retTargets", "Unsupported parent reference. No targets will be retrieved");
                }
                break;
            case TOP_ID:
                break;
            case CONJUNCT_ID:
                // Do cross product between uiid1 and uiid2 targets
                for (ViewTarget target1: retrieveTargets(uiid.getUiid1()) ) {
                    for (ViewTarget target2: retrieveTargets(uiid.getUiid1()) ) {
                        if(target1.targetType == ViewTargetType.Matcher && target2.targetType == ViewTargetType.Matcher) {
                            Matcher<View> matcher1 = ((ViewMatcherTarget) target1).matcher;
                            Matcher<View> matcher2 = ((ViewMatcherTarget) target2).matcher;
                            targets.add( mkMatcherTarget(allOf(matcher1,matcher2)));
                        } else {
                            Log.e("ChimpDriver-retTargets", "Unsupported conjunction reference. No targets will be retrieved");
                            return targets;
                        }
                    }
                }
                break;
            default:
        }

        return targets;
    }

}
