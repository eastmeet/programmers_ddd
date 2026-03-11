package eastmeet.backend5.member.presentation.controller;

import static eastmeet.backend5.util.GlobalConstant.REFRESH_TOKEN_HEADER;

import eastmeet.backend5.member.application.usecase.MemberUseCase;
import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberLoginReq;
import eastmeet.backend5.member.presentation.dto.res.MemberTokenRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberAuthControllerImpl implements MemberAuthController {

    private final MemberUseCase memberUseCase;

    @Override
    public ResponseEntity<Void> join(MemberJoinReq req) {
        memberUseCase.join(req);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Boolean> login(MemberLoginReq req) {
        MemberTokenRes result = memberUseCase.login(req);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(result.accessToken());
        headers.set(REFRESH_TOKEN_HEADER, result.refreshToken());

        return ResponseEntity.ok()
            .headers(headers)
            .body(result.isLogin());
    }

}
