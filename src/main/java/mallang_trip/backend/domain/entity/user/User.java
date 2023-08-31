package mallang_trip.backend.domain.entity.user;

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
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, updatable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Country country;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "phone_number", nullable = false, unique = true, updatable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "refresh_token")
    private String refreshToken;

    private String introduction;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(columnDefinition = "TINYINT", length = 1)
    @Builder.Default()
    private Boolean suspended= false;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (id == null) return false;
        if (!(o instanceof User))
            return false;

        final User user = (User)o;

        return id.equals(user.getId());
    }
}
