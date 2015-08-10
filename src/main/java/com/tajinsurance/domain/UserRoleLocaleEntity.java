package com.tajinsurance.domain;

import javax.persistence.*;

/**
 * Created by berz on 09.04.14.
 */
@Entity
@Table
@DiscriminatorValue("UserRoleLocaleEntity")
public class UserRoleLocaleEntity extends LocaleEntity {


    public UserRoleLocaleEntity() {
    }

    public UserRoleLocaleEntity(String locale) {
        this.locale = locale;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ur_id")
    private UserRole userRole;

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserRoleLocaleEntity && getId().equals(((UserRoleLocaleEntity) obj).getId());
    }

    @Override
    public String toString(){
        return getValue();
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }
}
