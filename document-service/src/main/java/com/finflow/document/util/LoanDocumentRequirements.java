package com.finflow.document.util;

import com.finflow.document.model.DocumentType;
import com.finflow.document.model.LoanType;

import java.util.List;

public class LoanDocumentRequirements {

    public static List<DocumentType> getRequiredDocs(LoanType loanType) {

        return switch (loanType) {

            case EDUCATION -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.STUDENT_ID,
                    DocumentType.ADMISSION_LETTER
            );

            case HOME -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.PROPERTY_DOCUMENT,
                    DocumentType.LAND_REGISTRATION,
                    DocumentType.INCOME_PROOF
            );

            case BUSINESS -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.GST_CERTIFICATE,
                    DocumentType.BUSINESS_PROOF,
                    DocumentType.INCOME_PROOF
            );

            case VEHICLE -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.VEHICLE_QUOTATION,
                    DocumentType.INCOME_PROOF
            );

            case MARRIAGE -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.MARRIAGE_PROOF,
                    DocumentType.INCOME_PROOF
            );

            case PERSONAL -> List.of(
                    DocumentType.AADHAR,
                    DocumentType.PAN,
                    DocumentType.INCOME_PROOF
            );
        };
    }
}