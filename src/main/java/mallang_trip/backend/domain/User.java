package mallang_trip.backend.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String nickname;
    String role;
    String password;
    String refreshToken;

    @Builder
    public User(String name, String nickname, String role, String password, String refreshToken) {
        this.name = name;
        this.nickname = nickname;
        this.role = role;
        this.password = password;
        this.refreshToken = refreshToken;
    }
}
