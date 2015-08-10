package com.tajinsurance.utils;

import com.tajinsurance.domain.User;
import com.tajinsurance.domain.UserRole;
import com.tajinsurance.dto.AjaxUserListFilter;
import com.tajinsurance.exceptions.BadNewUserDataException;
import com.tajinsurance.exceptions.IllegalDataException;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

/**
 * Created by berz on 27.03.14.
 */
public interface UserLoginUtil {
    public User getCurrentLogInUser();

    @Secured({"ROLE_USER","ROLE_ADMIN","ROLE_ADMIN_PARTNER"})
    public List<UserRole> getAllowedRolesForUserToCreate(User user);

    @Secured({"ROLE_USER","ROLE_ADMIN", "ROLE_ADMIN_PARTNER"})
    public List<UserRole> getAllowedRolesForUserToCreateLocalizated(User user, String locale);

    public List<UserRole> getclientManagerRoles();

    public List<UserRole> getclientManagerRolesLocalizated(String locale);

    public List<UserRole> getRolesLocalizations(List<UserRole> userRoles, String locale);

    public UserRole getUserRoleById(Long id);

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    public void createUser(User user);

    public void validateNewUser(User user) throws BadNewUserDataException, IllegalDataException;

    public UserRole getAuthorityByCode(UserRole.AuthorityCode code);

    public boolean isUserHasAuthority(User user, UserRole.AuthorityCode code);

    List<User> getUsersList(Long partnerId, List<UserRole.AuthorityCode> userRoles, Boolean locked, String locale);

    @Secured(value = {"ROLE_ADMIN","ROLE_USER","ROLE_ADMIN_PARTNER"})
    public List<User> getUsers(Long partnerId, List<String> userRoles, Boolean locked, String locale);

    public UserRole.AuthorityCode getMaxUserAuthorityCode(User user);

    public User getUserById(Long id);

    public String changePass(String pass) throws BadNewUserDataException;

    public void disableUser(User user);

    public void restoreUser(User user);

    void updateUser(User user) throws IllegalDataException;

    List<User> getUsers(Long partnerId, List<String> roles, Boolean locked, AjaxUserListFilter ajaxFilter, String language);

    List<User> getLocalizated(List<User> resultList, String locale);
}
