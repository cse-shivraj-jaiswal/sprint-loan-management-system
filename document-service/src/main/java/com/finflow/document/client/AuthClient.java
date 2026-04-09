package com.finflow.document.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "AUTH-SERVICE") // 🔥 Eureka service name
public interface AuthClient {

    @GetMapping("/auth/user/email/{email}")
    Long getUserIdByEmail(
            @RequestHeader("Authorization") String token,
            @PathVariable String email
    );
}