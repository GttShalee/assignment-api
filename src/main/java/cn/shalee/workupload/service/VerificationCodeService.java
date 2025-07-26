package cn.shalee.workupload.service;

/**
 * @author 31930
 */
public interface VerificationCodeService {
    void sendCode(String email);
    boolean verifyCode(String email, String code);
}