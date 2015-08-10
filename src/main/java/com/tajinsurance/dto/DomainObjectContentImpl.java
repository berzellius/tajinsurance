package com.tajinsurance.dto;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

/**
 * Created by berz on 17.06.14.
 */
public abstract class DomainObjectContentImpl {

    public <T extends Serializable> void setFields(T domainObject) {
        try {
            for (PropertyDescriptor propertyDescriptor :
                    Introspector.getBeanInfo(domainObject.getClass()).getPropertyDescriptors()) {
                try {
                    Field f = this.getClass().getField(propertyDescriptor.getName());

                    if(propertyDescriptor.getPropertyType().equals(f.getType())){
                        f.set(this, propertyDescriptor.getReadMethod().invoke(domainObject));
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    //e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

}
