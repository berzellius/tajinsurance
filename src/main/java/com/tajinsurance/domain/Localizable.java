package com.tajinsurance.domain;

import java.util.List;

/**
 * Created by berz on 17.04.14.
 */
public interface Localizable {

    public List<? extends LocaleEntity> getLocaleEntityList();

    public void setLocaleEntityList(List<? extends  LocaleEntity> localeEntityList);

}
