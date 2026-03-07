package eastmeet.backend5.member.application.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String memberInfo) {
        super("Member not found. memberInfo=" + memberInfo);
    }
}