package com.github.xwshop.service;

import com.github.xwshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TelVerificationServiceTest {

    private static TelVerificationService telVerificationService = new TelVerificationService();

    private static final AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode("13812345678", null);
    private static final AuthController.TelAndCode INVALID_PARAMETER = new AuthController.TelAndCode("138123", "null");
    private static final AuthController.TelAndCode EMPTY_TEL = new AuthController.TelAndCode(null, null);
    private static final AuthController.TelAndCode EMPTY = null;

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(telVerificationService.verifyTelParameter(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfInvalid() {
        Assertions.assertFalse(telVerificationService.verifyTelParameter(INVALID_PARAMETER));
    }

    @Test
    public void returnFalseIfTelEmpty() {
        Assertions.assertFalse(telVerificationService.verifyTelParameter(EMPTY_TEL));
    }

    @Test
    public void returnFalseIfEmpty() {
        Assertions.assertFalse(telVerificationService.verifyTelParameter(EMPTY));
    }
}
