package com.github.xwshop.entity;

import com.github.xwshop.generate.User;

public class LoginResponse {
    private boolean login;
    private User user;

    public static LoginResponse login(User user) {
        return new LoginResponse(true, user);
    }

    public static LoginResponse notLogin() {
        return new LoginResponse(false, null);
    }

    private LoginResponse(boolean login, User user) {
        this.login = login;
        this.user = user;
    }

    public boolean isLogin() {
        return login;
    }

    public User getUser() {
        return user;
    }
}
