package com.tajinsurance.utils;

import com.tajinsurance.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Created by berz on 26.03.14.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        Query q = entityManager.createQuery("SELECT o FROM User o WHERE username = :u", User.class).setParameter("u", s);

        User user = (User) q.getSingleResult();

        if(user == null) throw new UsernameNotFoundException("User " + s + " not found!");
        else return user;

    }
}
