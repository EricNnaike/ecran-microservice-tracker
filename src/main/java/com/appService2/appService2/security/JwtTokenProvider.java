package com.appService2.appService2.security;

import com.appService2.appService2.entity.Role;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

//    @Value("${jwt.secret.key}")
    private final String jwtSecret = "secretKey123";

    private final UserRepository userRepository;

    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findUserByEmail(username);
        String name = user == null ? "" : user.getFirstName() + " " + user.getLastName();
        Date currentDate = new Date();
        long jwtExpirationInMillis = 30 * 60 * 1000;
        Date expirationDate = new Date(currentDate.getTime() + jwtExpirationInMillis);
        Set<Role> roles = user.getRoles();
        List<String> roleName = roles.stream().map(role -> role.getRole()).collect(Collectors.toList());
        log.info("rolde obj {}", user.getRoles());
        log.info("roles {}", roleName);
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("name", name))
                .addClaims(Map.of("roles", roleName))
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generatePasswordResetToken(String email){
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + 900000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)

                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromJwt(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException exception){
            log.info("invalid jwt {}", exception.getLocalizedMessage());
            return false;
//            throw new RuntimeException("Invalid JWT_Token");
        }
    }
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

}

