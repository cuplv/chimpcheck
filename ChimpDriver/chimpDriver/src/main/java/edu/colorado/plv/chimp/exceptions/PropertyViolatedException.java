package edu.colorado.plv.chimp.exceptions;

import chimp.protobuf.Property;

/**
 * Created by edmund on 3/30/17.
 */
public class PropertyViolatedException extends Exception {

    Property.Prop violatedProp;

    public PropertyViolatedException(String msg, Property.Prop prop) {
        super(msg); violatedProp = prop;
    }

    public Property.Prop getProp() { return violatedProp; }
}
