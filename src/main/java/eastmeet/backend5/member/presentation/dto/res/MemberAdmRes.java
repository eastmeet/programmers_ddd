package eastmeet.backend5.member.presentation.dto.res;

import eastmeet.backend5.member.domain.model.Member;
import eastmeet.backend5.member.presentation.dto.type.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberAdmRes(UUID id, String email, String name, String phone, String address,
                           MemberStatus status, UUID regId, LocalDateTime regDt,
                           UUID modifyId, LocalDateTime modifyDt, String flag) {

    public static MemberAdmRes from(Member member) {
        return new MemberAdmRes(member.getId(), member.getEmail(), member.getName(), member.getPhone(), member.getAddress(),
            member.getStatus(), member.getRegId(), member.getRegDt(), member.getModifyId(), member.getModifyDt(), member.getFlag()
        );
    }
}
