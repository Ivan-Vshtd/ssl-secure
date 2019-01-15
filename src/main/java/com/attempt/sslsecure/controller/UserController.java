package com.attempt.sslsecure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * @author iveshtard
 * @since 1/11/2019
 */

@RestController
public class UserController {

    @Autowired
    WebClient webClient;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/done")
    public Mono<UserDetails> user(Principal principal) {
        //UserDetails currentUser = (UserDetails)((Authentication)principal).getPrincipal();
        return webClient.get().uri("/user").retrieve().bodyToMono(UserDetails.class);
    }
}
