package eastmeet.backend5.member.infra.persistence;

import eastmeet.backend5.member.domain.model.Member;
import eastmeet.backend5.member.domain.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepoAdapter implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public void save(Member member) {
        memberJpaRepository.save(member);
    }

    @Override
    public List<Member> findAll() {
        return memberJpaRepository.findAll();
    }

    @Override
    public Optional<Member> findById(UUID id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public boolean existByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existByPhone(String phone) {
        return memberJpaRepository.existsByPhone(phone);
    }

}
