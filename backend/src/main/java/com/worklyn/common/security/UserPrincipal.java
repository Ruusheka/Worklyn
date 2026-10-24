package com.worklyn.common.security;

import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final String id, email, passwordHash, employeeId;
    private final List<GrantedAuthority> authorities;
    private final boolean enabled;
    public UserPrincipal(String id, String email, String passwordHash, String employeeId, Collection<Role> roles, boolean enabled) {
        this.id=id; this.email=email; this.passwordHash=passwordHash; this.employeeId=employeeId; this.enabled=enabled;
        this.authorities=roles.stream().map(r -> new SimpleGrantedAuthority(r.name())).map(a -> (GrantedAuthority)a).toList();
    }
    public static UserPrincipal from(User user) { return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getEmployeeId(), user.getRoles(), user.isEnabled()); }
    public String getId(){return id;} public String getEmail(){return email;} public String getEmployeeId(){return employeeId;}
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){return authorities;}
    @Override public String getPassword(){return passwordHash;}
    @Override public String getUsername(){return email;}
    @Override public boolean isAccountNonExpired(){return true;}
    @Override public boolean isAccountNonLocked(){return true;}
    @Override public boolean isCredentialsNonExpired(){return true;}
    @Override public boolean isEnabled(){return enabled;}
}
