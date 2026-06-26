package com.arsalan.tenanttable.auth.service;

import com.arsalan.tenanttable.auth.enums.OtpPurpose;
import com.arsalan.tenanttable.user.entity.User;

public interface IOtpService {
    void generateOtp(User user, OtpPurpose purpose);
    void verifyOtp(User user, String otp, OtpPurpose purpose);
    void resendOtp(User user, OtpPurpose purpose);
}
