package mallang_trip.backend.domain.entity;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import mallang_trip.backend.constant.ArticleType;
import mallang_trip.backend.constant.Country;
import mallang_trip.backend.constant.Gender;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.domain.entity.BaseEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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
    Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    String loginId;

    @Column(nullable = false)
    String password;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true, updatable = false)
    String name;

    @Column(nullable = false)
    LocalDate birthday;

    @Enumerated(EnumType.STRING)
    Country country;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @Column(nullable = false, unique = true)
    String nickname;

    @Column(name = "phone_number", nullable = false, unique = true, updatable = false)
    String phoneNumber;

    @Enumerated(EnumType.STRING)
    Role role;

    @Column(name = "refresh_token")
    String refreshToken;

    String introduction;

    @Column(name = "profile_image")
    String profileImage;

    @Column(columnDefinition = "TINYINT", length = 1)
    @Builder.Default()
    Boolean suspended= false;

}
