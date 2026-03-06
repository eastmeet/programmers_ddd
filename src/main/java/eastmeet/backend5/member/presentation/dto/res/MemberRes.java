package eastmeet.backend5.member.presentation.dto.res;

import eastmeet.backend5.member.domain.model.Member;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MemberRes(UUID id, String name, String address) {

    public static MemberRes from(@NotNull Member member) {
        return new MemberRes(member.getId(), member.getName(), member.getAddress());
    }
}
