package eastmeet.backend5.member.application.usecase;

import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import java.util.List;
import java.util.UUID;

public interface MemberUseCase {

    void join(MemberJoinReq req);

    List<MemberRes> findAll();

    List<MemberAdmRes> findAdmAll();

    void stop(UUID id);

    void update(UUID id, MemberUpdateReq req);

}
