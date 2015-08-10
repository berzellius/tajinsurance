package com.tajinsurance.domain;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by berz on 26.03.14.
 */
@Entity
@Table(name = "user_roles")
public class UserRole implements GrantedAuthority, Localizable, Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_roles_id_generator")
    @SequenceGenerator(name = "user_roles_id_generator", sequenceName = "user_roles_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Override
    public List<UserRoleLocaleEntity> getLocaleEntityList() {
        return localeEntityList;
    }

    @Override
    public void setLocaleEntityList(List<? extends LocaleEntity> localeEntityList) {
        this.localeEntityList = (List<UserRoleLocaleEntity>) localeEntityList;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public enum AuthorityCode{
        ROLE_ANONYMOUS,
        ROLE_USER_PARTNER,
        ROLE_ADMIN_PARTNER,
        ROLE_USER,
        ROLE_ADMIN
    }

    private Integer lvl;

    public Integer getLvl() {
        return lvl;
    }

    public void setLvl(Integer lvl) {
        this.lvl = lvl;
    }

    //private String authority;
    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AuthorityCode authority;

    public UserRole(){}

    public AuthorityCode getAuthorityCode(){
        return this.authority;
    }

    @Override
    public String getAuthority() {
        return this.authority.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAuthority(AuthorityCode authority) {
        this.authority = authority;
    }

    @OneToMany(mappedBy = "userRole", fetch = FetchType.LAZY)
    private List<UserRoleLocaleEntity> localeEntityList;

    @Transient
    public String value;

    @Override
    public String toString(){
        return getValue() == "" ? getAuthority().toString() : getValue();
    }

    @Override
    public boolean equals(Object obj){
        return getId().equals(((UserRole) obj).getId()) && obj instanceof UserRole;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

}
