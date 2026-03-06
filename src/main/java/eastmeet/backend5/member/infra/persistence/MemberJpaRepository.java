package eastmeet.backend5.member.infra.persistence;

import eastmeet.backend5.member.domain.model.Member;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
