package com.github.xwshop.service;

import org.springframework.stereotype.Service;

@Service
public class MockSmsCodeService implements SmsCodeService {
    // 假设这是一个验证码服务
    @Override
    public String sendSmsCode(String tel) {
        // 所有的人收到的验证码都是"000000"
        return "000000";
    }
    // 验证码服务需要考虑的东西：
    // 我们需要做限流服务，如果不限流：
    // 1.短信服务变成了攻击别人的工具
    // 2.发送短信验证码是需要花钱的
    // 3.如果不限流，那么例如找回密码，就可以使用暴力破解
}
