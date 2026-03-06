package eastmeet.backend5.member.application.exception;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(UUID memberId) {
        super("Member not found. memberId=" + memberId);
    }
}