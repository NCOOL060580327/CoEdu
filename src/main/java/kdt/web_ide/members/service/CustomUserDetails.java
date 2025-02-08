package kdt.web_ide.members.service;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kdt.web_ide.members.entity.Member;
import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Member member;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  public Member getMember() {
    System.out.println("getMember ... function call");
    return member;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return member.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return member.getNickName();
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
    return true;
  }
}
