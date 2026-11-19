package com.MonitoreoViru.services;

import com.MonitoreoViru.config.ViruProperties;
import com.MonitoreoViru.models.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final ViruProperties props;

    public JwtService(JwtEncoder encoder, ViruProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public String generarToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("viru-backend")
                .issuedAt(now)
                .expiresAt(now.plus(props.jwt().expirationMinutes(), ChronoUnit.MINUTES))
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}