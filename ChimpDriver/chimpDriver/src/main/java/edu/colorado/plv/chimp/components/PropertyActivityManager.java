package edu.colorado.plv.chimp.components;

/**
 * Created by edmund on 3/30/17.
 */
import android.view.View;
import chimp.protobuf.Property;
import edu.colorado.plv.chimp.exceptions.MalformedBuiltinPredicateException;
import org.hamcrest.Matcher;

import android.support.test.espresso.matcher.ViewMatchers;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PropertyActivityManager extends ActivityManager {

   protected static final Map<String, Matcher<View>> ViewBuiltInPredicates = createMap();
   protected static Map<String, Matcher<View>>  createMap() {
        Map<String, Matcher<View>> thisMap = new HashMap<String, Matcher<View>>();

        thisMap.put("isClickable", ViewMatchers.isClickable());
        thisMap.put("isDisplayed", ViewMatchers.isDisplayed());
        thisMap.put("isEnabled", ViewMatchers.isEnabled());
        thisMap.put("supportsInputMethods", ViewMatchers.supportsInputMethods());
        thisMap.put("isSelected", ViewMatchers.isSelected());

        return thisMap;
   }

    public class PropResult {
       public boolean success = false;
       public Property.Prop violatedProp = null;
       public Property.BaseProp violatedBaseProp = null;
       public PropResult() { success = true; }
       public PropResult(Property.Prop violatedProp) {
           this.violatedProp = violatedProp;
       }
       public PropResult(Property.BaseProp violatedBaseProp) {
           this.violatedBaseProp = violatedBaseProp;
       }
   }

   protected PropResult success() { return new PropResult(); }
   protected PropResult violatedProp(Property.Prop prop) { return new PropResult(prop); }
   protected PropResult violatedProp(Property.BaseProp baseProp) { return new PropResult(baseProp); }

   protected PropResult check(Property.Pred predicate) throws MalformedBuiltinPredicateException {
       // Check if predicate is a ViewBuiltinPredicate
       if( ViewBuiltInPredicates.containsKey(predicate.getName()) ) {
           Matcher<View> predicateMatcher = ViewBuiltInPredicates.get(predicate.getName());
           if(predicate.getArgsCount() != 1) {
               String msg = String.format("Builtin predicate %s has wrong number of arguments: Suppose to be 1 instead of %s.",
                       predicate.getName(), predicate.getArgsCount());
               throw new MalformedBuiltinPredicateException(msg);
           }
           Property.Arg arg = predicate.getArgs(0);
           if(arg.getArgType() == Property.Arg.ArgType.BOOL_ARG) {
               String msg = String.format("Builtin predicate %s has wrong argumemt type: Boolean",
                       predicate.getName());
               throw new MalformedBuiltinPredicateException(msg);
           }
           int count = 0;
           switch (arg.getArgType()) {
               case INT_ARG:
                   count = getAllViews( allOf( withId(arg.getIntVal()) , predicateMatcher ) ).size();
                   break;
               case STR_ARG:
                   count = getAllViews( allOf( withText(arg.getStrVal()) , predicateMatcher ) ).size();
                   break;
           }
           if (count == 0) {
               return violatedProp(Property.BaseProp.newBuilder().setPropType(Property.BaseProp.BasePropType.PRIM_TYPE)
                       .setPred(predicate).build() );
           } else {
               return success();
           }
       }
       // TODO: Implement reflection call on top-level Chimp Driver
       return success();
   }

   protected PropResult check(Property.BaseProp baseProp) throws MalformedBuiltinPredicateException {
       switch (baseProp.getPropType()) {
           case BOT_TYPE: return violatedProp(baseProp);
           case TOP_TYPE: return success();
           case CONJ_BASE_TYPE:
               PropResult p1 = check( baseProp.getProp1() );
               if (p1.success) {
                   PropResult p2 = check( baseProp.getProp2() );
                   if(p2.success) {
                       return success();
                   } else {
                       return violatedProp(p2.violatedBaseProp);
                   }
               } else {
                   return violatedProp(p1.violatedBaseProp);
               }
           case DISJ_BASE_TYPE:
               PropResult d1 = check( baseProp.getProp1() );
               if (d1.success) {
                   return success();
               } else {
                   PropResult d2 = check( baseProp.getProp2() );
                   if (d2.success) {
                       return success();
                   } else {
                       return violatedProp(baseProp);
                   }
               }
           case PRIM_TYPE:
               PropResult p = check( baseProp.getPred() );
               if (p.success) {
                   return success();
               } else {
                   return violatedProp(baseProp);
               }
           case NEG_TYPE:
               PropResult n = check( baseProp.getProp1() );
               if (!n.success) {
                   return success();
               } else {
                   return violatedProp(baseProp);
               }
       }
       return null;
   }

   protected PropResult check(Property.Prop prop) throws MalformedBuiltinPredicateException {
       switch(prop.getPropType()) {
           case LIT_TYPE:
               PropResult litRes = check( prop.getPrem() );
               if(litRes.success) {
                 return success();
               } else {
                 return violatedProp(prop);
               }
           case IMP_TYPE:
               PropResult primRes = check( prop.getPrem() );
               if (primRes.success) {
                   PropResult concRes = check( prop.getConc() );
                   if (concRes.success) {
                       return success();
                   } else {
                       return violatedProp(prop);
                   }
               } else {
                   return success();
               }
           case CONJ_TYPE:
               PropResult c1 = check(prop.getProp1());
               if(c1.success) {
                   PropResult c2 = check(prop.getProp2());
                   if (c2.success) {
                       return success();
                   } else {
                       return violatedProp(prop.getProp2());
                   }
               } else {
                   return violatedProp(prop.getProp1());
               }
           case DISJ_TYPE:
               PropResult d1 = check(prop.getProp1());
               if (d1.success) {
                   return success();
               } else {
                   PropResult d2 = check(prop.getProp2());
                   if (d2.success) {
                       return success();
                   } else {
                       return violatedProp(prop);
                   }
               }
       }
       return success();
   }

}
