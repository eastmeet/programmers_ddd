package eastmeet.backend5.member.presentation.controller;

import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberLoginReq;
import eastmeet.backend5.util.PublicApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member Auth", description = "사용자 회원가입 및 인증 API")
@RequestMapping("/auth/v1/members")
public interface MemberAuthController {

    @PublicApi
    @PostMapping
    ResponseEntity<Void> join(@RequestBody @Valid MemberJoinReq req);

    @PublicApi
    @PostMapping("/login")
    ResponseEntity<Boolean> login(@RequestBody @Valid MemberLoginReq req);

    @GetMapping("/check")
    Boolean check(@RequestParam("httpMethod") String httpMethod, @RequestParam("requestPath") String requestPath);
}
