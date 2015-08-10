package com.tajinsurance.dto;

import java.io.Serializable;

/**
 * Created by berz on 17.06.14.
 */
public interface DomainObjectContent {

    public <T extends Serializable> void setFields(T domainObject);

}
