package mallang_trip.backend.domain.user.entity;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import mallang_trip.backend.domain.user.constant.Country;
import mallang_trip.backend.domain.user.constant.Gender;
import mallang_trip.backend.domain.user.constant.Role;
import mallang_trip.backend.global.entity.BaseEntity;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String di;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Country country;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column
    private String introduction;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "customer_key", nullable = false, updatable = false, unique = true)
    @Builder.Default()
    private String customerKey = UUID.randomUUID().toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.getId());
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.intValue() : 0;
        return result;
    }

    public Integer getAgeRange() {
        int age = LocalDate.now().getYear() - this.birthday.getYear();
        return (int) (age / 10) * 10;
    }

    // nickname Getter 재정의
    public String getNickname() {
        return this.role == Role.ROLE_DRIVER ? this.name + " 드라이버" : this.nickname;
    }
}
