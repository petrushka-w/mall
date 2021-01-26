package com.mall.user;

import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;

public interface IRegisterService {
    /**
     * 用户注册接口
     */
    UserRegisterResponse register(UserRegisterRequest registerRequest);
}
