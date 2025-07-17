package ru.red.lazaruscloud.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.red.lazaruscloud.security.Role;

import java.util.Collection;
import java.util.List;

public class LazarusUserDetail implements UserDetails {

    @Getter
    private final long id;
    private final String username;
    private final String password;

    @Getter
    private final String email;
    private final List<Role> authorities;
    @Getter
    private final String rootFolder;

    public LazarusUserDetail(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getRoles();
        this.rootFolder = user.getUsername()+"Root";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
