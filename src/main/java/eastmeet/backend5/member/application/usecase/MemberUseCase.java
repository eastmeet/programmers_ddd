package eastmeet.backend5.member.application.usecase;

import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberLoginReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import eastmeet.backend5.member.presentation.dto.res.MemberTokenRes;
import java.util.List;
import java.util.UUID;

public interface MemberUseCase {

    void join(MemberJoinReq req);

    MemberTokenRes login(MemberLoginReq req);

    List<MemberRes> findAll();

    List<MemberAdmRes> findAdmAll();

    void stop(UUID id);

    void update(UUID id, MemberUpdateReq req);

}
