package com.appsdeveloperblog.app.ws.security;

import com.appsdeveloperblog.app.ws.constants.SecurityConstants;
import com.appsdeveloperblog.app.ws.model.request.UserLoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
@Component
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    @Autowired
    public AuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager =authenticationManager;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            UserLoginRequestModel creds = new ObjectMapper()
                    .readValue(request.getInputStream(),UserLoginRequestModel.class);
            return authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                           creds.getEmail(),
                           creds.getPassword(),
                           new ArrayList<>()
                   )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String userName = ((User) authResult.getPrincipal()).getUsername();
        String token = Jwts.builder()
                .setSubject(userName)
                .setExpiration(new Date(System.currentTimeMillis()+ SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.ES512,SecurityConstants.TOKEN_SECRET)
                .compact();
        response.addHeader(SecurityConstants.HEADER_STRING,SecurityConstants.TOKEN_PREFIX+token);
    }


}
