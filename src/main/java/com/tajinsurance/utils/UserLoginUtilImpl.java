package com.tajinsurance.utils;

import com.tajinsurance.domain.User;
import com.tajinsurance.domain.UserRole;
import com.tajinsurance.domain.UserRoleLocaleEntity;
import com.tajinsurance.dto.AjaxUserListFilter;
import com.tajinsurance.exceptions.BadNewUserDataException;
import com.tajinsurance.exceptions.IllegalDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by berz on 27.03.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserLoginUtilImpl implements UserLoginUtil {
    @Autowired
    UserDetailsService userDetailsService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    CodeUtils codeUtils;


    @Override
    public User getCurrentLogInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        if (name == null) return null;

        return (User) userDetailsService.loadUserByUsername(name);
    }



    @Override
    public List<UserRole> getAllowedRolesForUserToCreate(User user) {
        //Integer maxLevel = getMaxUserLvl(user);

        Query q;
        switch (getMaxUserAuthorityCode(user)){
            case ROLE_USER:
                q = entityManager.createQuery("SELECT o FROM UserRole o WHERE lvl = 2 OR lvl = 3", UserRole.class);
            break;
            case ROLE_ADMIN:
                q = entityManager.createQuery("SELECT o FROM UserRole o WHERE lvl = 2 OR lvl = 3", UserRole.class);
            break;

            default:
                // такого не будет, так как вход сюда ограничен аннотацией @Secured
                q = entityManager.createQuery("SELECT o FROM UserRole o WHERE 1 = 2", UserRole.class);
            break;
        }

        List<UserRole> roles = q.getResultList();

        return roles;
    }

    @Override
    public List<UserRole> getAllowedRolesForUserToCreateLocalizated(User user, String locale) {
        return getRolesLocalizations(getAllowedRolesForUserToCreate(user), locale);
    }

    @Override
    public List<UserRole> getclientManagerRoles() {
        return entityManager.createQuery("SELECT o FROM UserRole o WHERE lvl = 2 OR lvl = 3", UserRole.class).getResultList();
    }

    @Override
    public List<UserRole> getclientManagerRolesLocalizated(String locale) {
        return getRolesLocalizations(getclientManagerRoles(), locale);
    }

    @Override
    public List<UserRole> getRolesLocalizations(List<UserRole> userRoles, String locale) {
        for (UserRole ur : userRoles) {
            for (UserRoleLocaleEntity url : ur.getLocaleEntityList()) {
                url = (UserRoleLocaleEntity) languageUtil.getLocalizatedObject(url, locale);
            }
        }

        return userRoles;
    }

    @Override
    public UserRole getUserRoleById(Long id) {
        UserRole ur = entityManager.find(UserRole.class, id);
        if (ur != null) return ur;
        else throw new EntityNotFoundException("no UserRole id=" + id);
    }

    @Override
    public void createUser(User user) {

        user.setEnabled(true);
        user.setLocked(false);
        user.setCredentialsExpired(false);
        user.setExpired(false);

        PasswordEncoder encoder = new Md5PasswordEncoder();
        user.setPassword(encoder.encodePassword(user.getPassword(), null));

        entityManager.persist(user);
    }

    @Override
    public UserRole getAuthorityByCode(UserRole.AuthorityCode code) {

        Query q = entityManager.createQuery("SELECT o FROM UserRole o WHERE authority = :a", UserRole.class);
        q.setParameter("a", code);

        return (UserRole) q.getSingleResult();
    }


    @Override
    public boolean isUserHasAuthority(User user, UserRole.AuthorityCode code) {
        UserRole ur = getAuthorityByCode(code);
        return user.getAuthorities().contains(ur);
    }


    @Override
    public List<User> getUsersList(Long partnerId, List<UserRole.AuthorityCode> userRoles, Boolean locked, String locale){
        String jpaQuery = "";//"SELECT o FROM User o ";

        if (partnerId != null) {
            jpaQuery += "WHERE partner.id = :p ";
        }

        if (locked != null) {
            jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "locked = :l ";
        }

        if(userRoles != null){
            jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "EXISTS(SELECT o1 from o.authorities o1 WHERE o1.authority IN (:auth)) ";
        }

        jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "username != 'guest'";


        Query q = entityManager.createQuery("SELECT o FROM User o " + jpaQuery, User.class);


        if (partnerId != null) q.setParameter("p", partnerId);

        if (locked != null) q.setParameter("l", locked);

        if(userRoles != null) q.setParameter("auth", userRoles);

        List<User> users = q.getResultList();




        for (User u : users) {
            //System.out.println("alive3");

            u = (User) languageUtil.getLocalizatedObject(u, locale);



            //System.out.println("users: " + users + " (" + users.size() + ")");

        }



        return users;
    }

    @Override
    public List<User> getUsers(Long partnerId, List<String> userRoles, Boolean locked, String locale) {

        String jpaQuery = "";//"SELECT o FROM User o ";

        if (partnerId != null) {
            jpaQuery += "WHERE partner.id = :p ";
        }

        if (locked != null) {
            jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "locked = :l ";
        }

        jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "username != 'guest'";


        Query q = entityManager.createQuery("SELECT o FROM User o " + jpaQuery, User.class);


        if (partnerId != null) q.setParameter("p", partnerId);

        if (locked != null) q.setParameter("l", locked);

        List<User> users = q.getResultList();




        for (User u : users) {
            //System.out.println("alive3");

            u = (User) languageUtil.getLocalizatedObject(u, locale);

            if (userRoles != null) {
                for (String r : userRoles) {
                    if (!isUserHasAuthority(u, UserRole.AuthorityCode.valueOf(r))) users.remove(u);
                }
            }

            //System.out.println("users: " + users + " (" + users.size() + ")");

        }


        return users;
    }

    @Override
    public void validateNewUser(User user) throws BadNewUserDataException, IllegalDataException {
        Query existsUser = entityManager.createQuery("SELECT o FROM User o WHERE username = :u")
                .setParameter("u", user.getUsername());

        if(existsUser.getResultList().size() > 0) throw new BadNewUserDataException("duplicate_username");

        if (user.getAuthorities() == null) throw new BadNewUserDataException("no_authorities");

        if (user.getAuthorities().size() == 0) throw new BadNewUserDataException("no_authorities");

        if (
                (
                        isUserHasAuthority(user, UserRole.AuthorityCode.ROLE_USER_PARTNER)
                        || isUserHasAuthority(user, UserRole.AuthorityCode.ROLE_ADMIN_PARTNER)
                )
                && user.getPartner() == null
            )
            throw new BadNewUserDataException("no_partner");

        if (user.getUsername() == "") throw new BadNewUserDataException("no_username");

        if (user.getPassword() == "") throw new BadNewUserDataException("no_password");

        if(!codeUtils.checkEmailIsValid(user.getEmail())) throw new IllegalDataException("wrong email: "+user.getEmail(), IllegalDataException.Reason.WRONG_EMAIL);

    }

    private Integer getMaxUserLvl(User user) {
        Integer max = 0;
        for (UserRole ur : user.getAuthorities()) {
            max = (ur.getLvl() > max) ? ur.getLvl() : max;
        }
        return max;
    }

    @Override
    public UserRole.AuthorityCode getMaxUserAuthorityCode(User user) {
        Query q = entityManager.createQuery("SELECT o FROM UserRole o WHERE lvl = :l", UserRole.class);
        q.setParameter("l", getMaxUserLvl(user));
        return ((UserRole) q.getSingleResult()).getAuthorityCode();
    }

    @Override
    public User getUserById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public String changePass(String pass) throws BadNewUserDataException {
        //User user = entityManager.find(User.class, userId);

        //if (user == null) throw new IllegalArgumentException();

        if (pass == "") throw new BadNewUserDataException("no_password");

        if (pass.length() < 8) throw new BadNewUserDataException("short_password");

        PasswordEncoder encoder = new Md5PasswordEncoder();
        //user.setPassword(encoder.encodePassword(user.getPassword(), null));
        return encoder.encodePassword(pass, null);
        //entityManager.persist(user);

    }

    @Override
    public void disableUser(User user) {


        user.setEnabled(false);

        entityManager.persist(user);

    }

    @Override
    public void restoreUser(User user) {


        user.setEnabled(true);

        entityManager.persist(user);

    }

    @Override
    public void updateUser(User user) throws IllegalDataException {

        if( !codeUtils.checkEmailIsValid(user.getEmail())) throw new IllegalDataException("This email is not valid: " + user.getEmail(), IllegalDataException.Reason.WRONG_EMAIL);

        entityManager.merge(user);
    }

    @Override
    public List<User> getUsers(Long partnerId, List<String> userRoles, Boolean locked, AjaxUserListFilter ajaxFilter, String locale) {
        String jpaQuery = "";//"SELECT o FROM User o ";

        if (partnerId != null) {
            jpaQuery += "WHERE partner.id = :p ";
        }

        if (locked != null) {
            jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "locked = :l ";
        }

        jpaQuery += ((jpaQuery == "") ? "WHERE " : "AND ") + "username != 'guest'";


        if(ajaxFilter != null){


            if(ajaxFilter.fio != null){

                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "LOWER(fio) LIKE LOWER(:fio) ";

            }

            if(ajaxFilter.login != null){
                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "LOWER(username) LIKE LOWER(:login) ";
            }

            if(ajaxFilter.partner != null){
                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "LOWER(partner.value) LIKE LOWER(:partner) ";
            }

            if(ajaxFilter.userRoles != null){
                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "EXISTS(SELECT o1 from o.authorities o1 WHERE o1.id IN (:auth))";
            }

            if(ajaxFilter.isEnabled != null){
                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "enabled = :enabled ";
            }

            if(ajaxFilter.email != null){
                jpaQuery += ((jpaQuery == "") ? " WHERE " : " AND ") + "LOWER(email) LIKE LOWER(:email)";
            }

            if(ajaxFilter.orderColumn != null){
                jpaQuery += " ORDER BY " + ajaxFilter.orderColumn + " ";

                jpaQuery += (ajaxFilter.orderType != null)? ajaxFilter.orderType : "DESC";
            }

        }



        Query q = entityManager.createQuery("SELECT o FROM User o " + jpaQuery, User.class);

        if (partnerId != null) q.setParameter("p", partnerId);

        if (locked != null) q.setParameter("l", locked);


        if(ajaxFilter != null){
            if(ajaxFilter.fio != null) q.setParameter("fio", ajaxFilter.fio+"%");

            if(ajaxFilter.login != null) q.setParameter("login", ajaxFilter.login+"%");

            if(ajaxFilter.partner != null) q.setParameter("partner", ajaxFilter.partner+"%");

            if(ajaxFilter.userRoles != null) q.setParameter("auth", ajaxFilter.userRoles);

            if(ajaxFilter.isEnabled != null) q.setParameter("enabled", ajaxFilter.isEnabled);

            if(ajaxFilter.email != null) q.setParameter("email", ajaxFilter.email+"%");
        }


        List<User> users = getLocalizated((List<User>) q.getResultList(), locale);

        for (User u : users) {
            //u = (User) languageUtil.getLocalizatedObject(u, locale);



            if (userRoles != null) {
                for (String r : userRoles) {
                    if (!isUserHasAuthority(u, UserRole.AuthorityCode.valueOf(r))) users.remove(u);
                }
            }


        }

        return users;
    }

    @Override
    public List<User> getLocalizated(List<User> resultList, String locale) {
        for(User u : resultList){
            u = (User) languageUtil.getLocalizatedObject(u, locale);
        }

        return  resultList;
    }
}
