package kdt.web_ide.members.service;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(userName)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new CustomUserDetails(member);
    }
}
