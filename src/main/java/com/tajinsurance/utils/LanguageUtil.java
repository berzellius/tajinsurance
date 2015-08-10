package com.tajinsurance.utils;

import org.springframework.stereotype.Service;

/**
 * Created by berz on 16.04.14.
 */
@Service
public interface LanguageUtil {



    public Object getLocalizatedObject(Object obj, String locale);


}
