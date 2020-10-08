package com.github.xwshop.controller;

import com.github.xwshop.context.UserContext;
import com.github.xwshop.entity.LoginResponse;
import com.github.xwshop.service.AuthService;
import com.github.xwshop.service.TelVerificationService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final TelVerificationService telVerificationService;

    @Autowired
    public AuthController(AuthService authService, TelVerificationService telVerificationService) {
        this.authService = authService;
        this.telVerificationService = telVerificationService;
    }

    @PostMapping("/code")
    public void code(@RequestBody TelAndCode telAndCode,
                     HttpServletResponse response) {
        if (telVerificationService.verifyTelParameter(telAndCode)) {
            authService.sendVerificationCode(telAndCode.getTel());
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody TelAndCode telAndCode) {
        // shiro 需要一个token来完成登录
        UsernamePasswordToken token = new UsernamePasswordToken(telAndCode.getTel(),
                telAndCode.getCode());
        // cookie
        token.setRememberMe(true);
        SecurityUtils.getSubject().login(token);
    }

    @PostMapping("/logout")
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    @GetMapping("/status")
    public void loginStatus(HttpServletResponse response) {
        response.setContentType("application/json");
        if (UserContext.getCurrentUser() == null) {
            LoginResponse loginResponse = LoginResponse.notLogin();
            response.setHeader("login", "false");
            // return loginResponse;
        } else {
            LoginResponse loginResponse = LoginResponse.login(UserContext.getCurrentUser());
            response.setHeader("login", "true");
            // return loginResponse;
        }
    }

    public static class TelAndCode {
        private String tel;
        private String code;

        public TelAndCode(String tel, String code) {
            this.tel = tel;
            this.code = code;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
