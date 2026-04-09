package com.finflow.document.dto;

import com.finflow.document.model.DocumentType;

import lombok.Data;

@Data
public class UploadDocumentRequest {

    private Long applicationId;
    private DocumentType documentType;
    private String fileUrl;
}
