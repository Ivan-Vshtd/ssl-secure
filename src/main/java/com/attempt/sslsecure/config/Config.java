package com.attempt.sslsecure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author iveshtard
 * @since 1/11/2019
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Config extends WebSecurityConfigurerAdapter {

    @Bean
    public UserDetailsService userDetailsService(){
        return userName -> {
            System.err.println(userName);
            if (userName.equals("cid")){
                return new User(userName, "",
                        AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            } else {
                throw new RuntimeException("The username check was failed!");
            }
        };

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and().csrf().disable()
                .formLogin().permitAll()
                .and()
                .x509()
                .subjectPrincipalRegex("CN=(.*?),")
                .userDetailsService(userDetailsService());
    }
}

