package kdt.web_ide.members.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String userId) {
    log.info("User Name = {}", userId);
    Member member =
        memberRepository
            .findByLoginId(userId)
            .orElseThrow(
                () -> {
                  log.error("사용자를 찾을 수 없습니다: {}", userId);
                  return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

    return new CustomUserDetails(member);
  }
}
