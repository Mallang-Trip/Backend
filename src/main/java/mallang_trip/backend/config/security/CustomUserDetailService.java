package mallang_trip.backend.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        log.info("user details at username = {}", id);
        return userRepository.findByLoginId(id)
                .map(user -> createUser(user))
                .orElseThrow(() -> new UsernameNotFoundException(id + "Not Found"));

//        return createUser(user);
    }

    /**Security User 정보를 생성한다. */
    private User createUser(mallang_trip.backend.domain.entity.user.User user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().toString());

        User result =  new User(
                String.valueOf(user.getId()),
                user.getPassword(),
                Collections.singletonList(grantedAuthority)
        );

        return result;
    }
}
