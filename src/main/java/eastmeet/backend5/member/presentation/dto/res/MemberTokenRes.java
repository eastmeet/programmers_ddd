package eastmeet.backend5.member.presentation.dto.res;

public record MemberTokenRes(
    Boolean isLogin,
    String accessToken,
    String refreshToken
) {

}
