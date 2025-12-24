package com.example.demo.data.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
public class OwnerUserDetails implements UserDetails {
    private final Owner owner;
    private final List<Authority> authorities;

    public OwnerUserDetails(Owner owner, List<Authority> authorities) {
        this.owner = owner;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .toList();
    }

    @Override
    public String getPassword() {return owner.getPassword();}

    @Override
    public String getUsername() {return owner.getUsername();}

    @Override
    public boolean isAccountNonExpired() {return true;}
    @Override
    public boolean isAccountNonLocked() {return true;}
    @Override
    public boolean isCredentialsNonExpired() {return true;}
    @Override
    public boolean isEnabled() {return true;}


}
