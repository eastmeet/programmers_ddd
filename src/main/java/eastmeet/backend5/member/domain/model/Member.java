package eastmeet.backend5.member.domain.model;

import eastmeet.backend5.member.presentation.dto.type.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Entity
@Getter
@Table(name = "\"member\"", schema = "public")
public class Member {

    @Id
    @Column(comment = "유저의 UUID")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true, comment = "유저의 이메일")
    private String email;

    @Column(name = "\"name\"", length = 20, comment = "유저명")
    private String name;

    @Column(name = "\"password\"", nullable = false, length = 100, comment = "비밀번호")
    private String password;

    @Column(nullable = false, length = 20, unique = true, comment = "핸드폰번호")
    private String phone;

    @Column(nullable = false, length = 100, comment = "주소")
    private String address;

    @Column(name = "\"status\"", length = 15, comment = "유저 상태")
    @Enumerated(value = EnumType.STRING)
    private MemberStatus status;

    @Column(name = "reg_id", nullable = false)
    private UUID regId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "modify_id", nullable = false)
    private UUID modifyId;

    @Column(name = "modify_dt", nullable = false)
    private LocalDateTime modifyDt;

    @Column(name = "saltkey", nullable = false, length = 25)
    private String saltKey;

    @Column(name = "flag", length = 5)
    private String flag;

    protected Member() {

    }

    private Member(String email, String name, String password, String phone, String address, MemberStatus status,
        LocalDateTime regDt, LocalDateTime modifyDt, String saltKey, String flag) {
        UUID id = UUID.randomUUID();
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.regId = id;
        this.regDt = regDt;
        this.modifyId = id;
        this.modifyDt = modifyDt;
        this.saltKey = saltKey;
        this.flag = flag;
    }

    public static Member create(String email, String name, String password, String phone, String address, String saltKey) {
        return new Member(email, name, password, phone, address, MemberStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(), saltKey, null);
    }

    public void update(String name, String password, String phone, String address) {
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }

        if (regId == null) {
            this.regId = id;
        }

        if (modifyId == null) {
            this.modifyId = regId;
        }

        if (regDt == null) {
            this.regDt = LocalDateTime.now();
        }

        if (modifyDt == null) {
            this.modifyDt = LocalDateTime.now();
        }

        if (status == null) {
            status = MemberStatus.INACTIVE;
        }

    }

    public void delete() {
        this.status = MemberStatus.DELETED;
    }
}
