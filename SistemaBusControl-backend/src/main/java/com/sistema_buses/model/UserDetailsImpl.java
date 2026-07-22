package com.sistema_buses.model;

import jakarta.annotation.Nonnull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserDetailsImpl(Usuario user) implements UserDetails {

	public Usuario user() {
		return user;
	}
	
    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRol().getNombre());
        return List.of(authority);
    }

    @Override
    @Nonnull
    public String getUsername() {
        return user.getCorreo();
    }

    @Override
    @Nonnull
    public String getPassword() {
        return user.getClave();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActivo();
    }

}
