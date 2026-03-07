package eastmeet.backend5.member.domain.repository;

import eastmeet.backend5.member.domain.model.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {

    void save(Member member);

    List<Member> findAll();

    Optional<Member> findById(UUID id);

    boolean existByEmail(String email);

    boolean existByPhone(String phone);

    Optional<Member> findByEmail(String email);
}

