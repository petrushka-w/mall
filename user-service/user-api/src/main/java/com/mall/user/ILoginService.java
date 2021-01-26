package com.mall.user;

import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;

public interface ILoginService {

    UserLoginResponse login(UserLoginRequest request);
    CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest);
}
