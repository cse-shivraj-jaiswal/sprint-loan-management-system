package com.finflow.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", path = "/auth")
public interface AuthClient {

    @GetMapping("/user/email/{email}")
    Long getUserIdByEmail(@PathVariable("email") String email);
}
