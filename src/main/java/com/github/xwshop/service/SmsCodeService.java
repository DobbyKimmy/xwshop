package com.github.xwshop.service;

import org.springframework.stereotype.Service;

@Service
public interface SmsCodeService {
    /**
     * 向一个指定手机号发验证码返回正确答案
     * @param tel 目标手机号
     * @return 返回正确答案
     */
    String sendSmsCode(String tel);
}