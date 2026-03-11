package eastmeet.backend5.member.application.service;

import eastmeet.backend5.member.application.exception.MemberEmailDuplicateException;
import eastmeet.backend5.member.application.exception.MemberNotFoundException;
import eastmeet.backend5.member.application.usecase.MemberUseCase;
import eastmeet.backend5.member.domain.model.Member;
import eastmeet.backend5.member.domain.repository.MemberRepository;
import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberLoginReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import eastmeet.backend5.member.presentation.dto.res.MemberTokenRes;
import eastmeet.backend5.util.JwtProvider;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberApplicationService implements MemberUseCase {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void join(MemberJoinReq req) {
        checkEmailDuplicate(req.email());
        checkPhoneDuplicate(req.phone());

        SecureRandom random = new SecureRandom();
        byte[] generateSeed = random.generateSeed(8);
        String saltKey = Base64.getEncoder().encodeToString(generateSeed);

        String encodedPassword = passwordEncoder.encode(req.password() + saltKey);

        Member member = Member.create(
            req.email(),
            req.name(),
            encodedPassword,
            req.phone(),
            req.address(),
            saltKey
        );

        memberRepository.save(member);
    }

    @Override
    public MemberTokenRes login(MemberLoginReq req) {
        Member member = getMemberByEmail(req.email());
        String rawPassword = req.password();
        String encodedPassword = member.getPassword();
        Assert.state(passwordEncoder.matches(rawPassword + member.getSaltKey(), encodedPassword), "비밀번호가 일치하지않습니다.");

        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getId(), null);
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);
        Boolean isLogin = accessToken == null ? Boolean.FALSE : Boolean.TRUE;

        return new MemberTokenRes(isLogin, accessToken, refreshToken);
    }

    @Override
    public List<MemberRes> findAll() {
        return memberRepository.findAll().stream()
            .map(MemberRes::from)
            .toList();
    }

    @Override
    public List<MemberAdmRes> findAdmAll() {
        return memberRepository.findAll().stream()
            .map(MemberAdmRes::from)
            .toList();
    }

    @Override
    @Transactional
    public void update(UUID id, MemberUpdateReq req) {
        Member member = getMemberById(id);
        member.update(req.name(), req.password(), req.phone(), req.address());
    }

    @Override
    @Transactional
    public void stop(UUID id) {
        Member member = getMemberById(id);
        member.delete();
    }

    private Member getMemberById(UUID id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id.toString()));
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new MemberNotFoundException(email));
    }

    private void checkEmailDuplicate(String email) {
        if (memberRepository.existByEmail(email)) {
            throw new MemberEmailDuplicateException(email);
        }
    }

    private void checkPhoneDuplicate(String phone) {
        if (memberRepository.existByPhone(phone)) {
            throw new MemberEmailDuplicateException(phone);
        }
    }

}
