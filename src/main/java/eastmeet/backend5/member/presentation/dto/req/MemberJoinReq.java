package eastmeet.backend5.member.presentation.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MemberJoinReq(

    @NotBlank
    @Schema(description = "유저 이메일")
    String email,

    @NotBlank
    @Schema(description = "유저명")
    String name,

    @NotBlank
    @Schema(description = "유저 비밀번호")
    String password,

    @NotBlank
    @Schema(description = "유저 핸드폰번호")
    String phone,

    @NotBlank
    @Schema(description = "유저 주소")
    String address

    ) {

}
