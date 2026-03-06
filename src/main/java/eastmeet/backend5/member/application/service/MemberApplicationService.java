package eastmeet.backend5.member.application.service;

import eastmeet.backend5.member.application.exception.MemberEmailDuplicateException;
import eastmeet.backend5.member.application.exception.MemberNotFoundException;
import eastmeet.backend5.member.application.usecase.MemberUseCase;
import eastmeet.backend5.member.domain.model.Member;
import eastmeet.backend5.member.domain.repository.MemberRepository;
import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberApplicationService implements MemberUseCase {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void join(MemberJoinReq req) {
        checkEmailDuplicate(req.email());
        checkPhoneDuplicate(req.phone());

        Member member = Member.create(
            req.email(),
            req.name(),
            req.password(),
            req.phone(),
            req.address()
        );
        memberRepository.save(member);
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
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
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
