package com.tajinsurance.domain;

import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by berz on 26.03.14.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails,Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_id_generator")
    @SequenceGenerator(name = "user_id_generator", sequenceName = "user_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;


    private String username;

    private String password;

    private String fio;

    private String memo;

    private String email;

    private boolean locked;

    private boolean expired;

    @Column(name = "credentials_expired")
    private boolean credentialsExpired;

    @OneToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToMany
    @JoinTable(
            name = "users_authorities",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "auth_id")
            }
    )
    private List<UserRole> authorities;

    public User() {
    }

    public User(boolean allEnabled) {
        if (!allEnabled) return;
        setExpired(false);
        setCredentialsExpired(false);
        setLocked(false);
        setEnabled(true);
    }


    @Override
    public List<UserRole> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isExpired();
    }

    public boolean isExpired() {
        return this.expired;
    }

    public void setExpired(boolean e) {
        this.expired = e;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked();
    }

    public void setLocked(boolean l) {
        this.locked = l;
    }

    public boolean isLocked() {
        return this.locked;
    }

    private boolean enabled;

    @Override
    public boolean isCredentialsNonExpired() {
        return !isCredentialsExpired();
    }

    public boolean isCredentialsExpired() {
        return this.credentialsExpired;
    }

    public void setCredentialsExpired(boolean ce) {
        this.credentialsExpired = ce;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public void setAuthorities(List<UserRole> authorities) {
        this.authorities = authorities;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /*@Version*/
    @Column(name = "version")
    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && getId().equals(((User) obj).getId());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
