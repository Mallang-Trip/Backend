package mallang_trip.backend.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.TokensDto;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static mallang_trip.backend.controller.io.BaseResponseStatus.*;

@Slf4j
@Component
@Transactional
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private Key key;
    private final UserRepository userRepository;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity-in-milliseconds}") long refreshTokenValidity,
            UserRepository userRepository
    ){
        this.secret = secret;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.userRepository = userRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Authentication 객체의 권한 정보를 이용해서 토큰을 생성
    public TokensDto createToken(Authentication authentication){
        // authorities 설정
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰 만료 시간 설정
        //long now = (new Date()).getTime();
        long now = System.currentTimeMillis();
        Date accessTokenValidity = new Date(now + this.accessTokenValidity);
        Date refreshTokenValidity = new Date(now + this.refreshTokenValidity);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(accessTokenValidity)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(refreshTokenValidity)
                .compact();
        Optional<mallang_trip.backend.domain.entity.user.User> findUser = userRepository.findById(Long.valueOf(authentication.getName()));
        findUser.get().setRefreshToken(refreshToken);

        return new TokensDto(accessToken, refreshToken);
    }

    // 토큰에 담겨있는 정보를 이용해 Authentication 객체 리턴
    public Authentication getAuthentication(String token) {
        // 토큰을 이용하여 claim 생성
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // claim을 이용하여 authorities 생성
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // claim과 authorities 이용하여 User 객체 생성
        User principal = new User(claims.getSubject(), "", authorities);

        // 최종적으로 Authentication 객체 리턴
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 검증
    public boolean validateToken(String token, HttpServletRequest request){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("변조된 JWT 토큰입니다.");
            request.setAttribute("exception", "10002");
        }
        catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            request.setAttribute("exception", "10003");
        }
        catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            request.setAttribute("exception", "10002");
        }
        catch (IllegalArgumentException e) {
            log.info("잘못된 JWT 토큰입니다.");
            request.setAttribute("exception", "10002");
        }

        return false;
    }

    public TokensDto doRefresh() {
        try {
            String refreshToken = getRefreshToken();

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken);

            Authentication authentication = getAuthentication(refreshToken);
            Optional<mallang_trip.backend.domain.entity.user.User> findUser = userRepository.findById(Long.valueOf(authentication.getName()));
            if (!findUser.isPresent()) {
                throw new BaseException(INVALID_JWT);
            }
            if (!findUser.get().getRefreshToken().equals(refreshToken)) {
                throw new BaseException(INVALID_JWT);
            }

            TokensDto tokensDto = createToken(authentication);
            findUser.get().setRefreshToken(tokensDto.getRefreshToken());

            return tokensDto;
        }
        catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        }
        catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_JWT);
        }
        catch (UnsupportedJwtException e) {
            throw new BaseException(INVALID_JWT);
        }
        catch (IllegalArgumentException e) {
            throw new BaseException(INVALID_JWT);
        }
    }

    public String getAccessToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("ACCESS-TOKEN").substring(7);
    }

    public String getRefreshToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("REFRESH-TOKEN").substring(7);
    }
}
