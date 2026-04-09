package com.finflow.application.client;

import com.finflow.application.model.LoanType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "DOCUMENT-SERVICE", path = "/documents")
public interface DocumentClient {

    @GetMapping("/validate/{applicationId}/{loanType}")
    boolean validateDocuments(
            @PathVariable("applicationId") Long applicationId,
            @PathVariable("loanType") LoanType loanType);
}
