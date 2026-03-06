package eastmeet.backend5.member.presentation.controller;

import eastmeet.backend5.member.presentation.dto.req.MemberJoinReq;
import eastmeet.backend5.member.presentation.dto.req.MemberUpdateReq;
import eastmeet.backend5.member.presentation.dto.res.MemberAdmRes;
import eastmeet.backend5.member.presentation.dto.res.MemberRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Member", description = "사용자 CRUD API")
@RequestMapping("/api/v1/members")
public interface MemberController {

    @PostMapping
    ResponseEntity<Void> join(@RequestBody @Valid MemberJoinReq req);

    @GetMapping
    ResponseEntity<List<MemberRes>> getAll();

    @GetMapping("/adm")
    ResponseEntity<List<MemberAdmRes>> getAdmAll();

    @PutMapping("/{id}")
    ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody @Valid MemberUpdateReq req);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> stop(@PathVariable UUID id);

}
