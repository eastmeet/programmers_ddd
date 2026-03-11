package eastmeet.backend5.member.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLoginReq(
    @Schema(example = "testor@test.com")
    String email,

    @Schema(example = "test")
    String password
) {

}
