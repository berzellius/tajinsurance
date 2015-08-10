package com.tajinsurance.utils;

import com.tajinsurance.domain.LocaleEntity;
import com.tajinsurance.domain.Localizable;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by berz on 16.04.14.
 */
@Service
public class LanguageUtilImpl implements LanguageUtil {
    @Override
    public Object getLocalizatedObject(Object obj, String locale) {
        return getOneLayerLocalizatedFields(obj, locale);
    }


    private Object getOneLayerLocalizatedFields(Object obj, String locale) {

        for (Field field : obj.getClass().getDeclaredFields()) {


            if (field.getName() == "localeEntityList"
                    && field.getType().equals(List.class)
                    && LocaleEntity.class.isAssignableFrom((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])) {

                try {

                    Method getlocaleEntitiesList = obj.getClass().getDeclaredMethod("getLocaleEntityList");

                    //List<? extends LocaleEntity> entitiesList = (List<? extends LocaleEntity>) field.get(obj);

                    List<? extends LocaleEntity> entitiesList = (List<? extends LocaleEntity>) getlocaleEntitiesList.invoke(obj);

                    Class<? extends LocaleEntity> localeObjectClass = (Class<? extends LocaleEntity>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];


                    for (PropertyDescriptor propertyDescriptor :
                            Introspector.getBeanInfo(localeObjectClass).getPropertyDescriptors()) {

                        if (propertyDescriptor.getPropertyType().equals(String.class)) {
                            Method getter = propertyDescriptor.getReadMethod();

                            Field f = null;
                            try {
                                f = obj.getClass().getField(propertyDescriptor.getName());
                            } catch (NoSuchFieldException e) {
                                // null
                            }

                            if (f != null) {
                                for (LocaleEntity localeEntity : entitiesList) {
                                    String propertyValue = (String) getter.invoke(localeEntity);

                                    if (propertyValue != null && localeEntity.getLocale().equals(locale))
                                        f.set(obj, propertyValue);
                                }
                            }
                        }
                    }


                } catch (IllegalAccessException e) {
                    // do nothing;
                } catch (NoSuchMethodException e) {
                    // do nothing
                } catch (InvocationTargetException e) {
                    // do nothing
                } catch (IntrospectionException e) {
                    // do nothing
                }
            }

            if (field.getType().equals(List.class)
                    && Localizable.class.isAssignableFrom(
                    (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]
            )) {

                try {
                    for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()) {

                        if (propertyDescriptor.getName().equals(field.getName())) {

                            Method readMethod = propertyDescriptor.getReadMethod();

                            if (readMethod != null) {
                                List<Object> objects = (List<Object>) readMethod.invoke(obj);

                                if (objects != null) {
                                    for (Object o : objects) {
                                        o = getOneLayerLocalizatedFields(o, locale);
                                    }
                                }
                            }

                            //propertyDescriptor.getWriteMethod().invoke(obj, objects);
                        }
                    }
                } catch (IntrospectionException e) {
                    //
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    //
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    //
                    e.printStackTrace();
                }

            }


            if (Localizable.class.isAssignableFrom(
                    field.getType()
            )) {

                try {
                    for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()) {

                        if (propertyDescriptor.getName().equals(field.getName())) {
                            propertyDescriptor.getWriteMethod().invoke(
                                    obj,
                                    getOneLayerLocalizatedFields(
                                            propertyDescriptor.getReadMethod().invoke(obj)
                                            , locale)
                            );
                        }

                    }
                } catch (IntrospectionException e) {
                    // do nothing
                } catch (InvocationTargetException e) {
                    //
                } catch (IllegalAccessException e) {
                    //
                }
            }

        }

        return obj;
    }

    ;
}
