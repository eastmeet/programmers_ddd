package eastmeet.backend5.member.presentation.controller;

import eastmeet.backend5.member.application.usecase.MemberUseCase;
import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberControllerImpl implements MemberController {

    private final MemberUseCase memberUseCase;

    public ResponseEntity<Void> join(MemberJoinReq req) {
        memberUseCase.join(req);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<MemberRes>> getAll() {
        List<MemberRes> result = memberUseCase.findAll();
        return ResponseEntity.ok().body(result);
    }

    public ResponseEntity<List<MemberAdmRes>> getAdmAll() {
        List<MemberAdmRes> result = memberUseCase.findAdmAll();
        return ResponseEntity.ok().body(result);
    }

    @Override
    public ResponseEntity<Void> update(UUID id, MemberUpdateReq req) {
        memberUseCase.update(id, req);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> stop(UUID id) {
        memberUseCase.stop(id);
        return ResponseEntity.ok().build();
    }

}
