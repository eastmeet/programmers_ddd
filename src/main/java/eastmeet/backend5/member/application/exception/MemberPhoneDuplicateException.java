package eastmeet.backend5.member.application.exception;

public class MemberPhoneDuplicateException extends RuntimeException {

    public MemberPhoneDuplicateException(String phone) {
        super("Member phone exists. phone=" + phone);
    }
}