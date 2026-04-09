package com.finflow.document.client;

import com.finflow.document.dto.LoanApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "APPLICATION-SERVICE")
public interface ApplicationClient {

    @GetMapping("/applications/{id}")
    LoanApplicationDTO getById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id
    );
}
