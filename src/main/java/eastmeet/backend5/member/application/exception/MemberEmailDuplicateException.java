package eastmeet.backend5.member.application.exception;

public class MemberEmailDuplicateException extends RuntimeException {

    public MemberEmailDuplicateException(String email) {
        super("Member email exists. email=" + email);
    }
}