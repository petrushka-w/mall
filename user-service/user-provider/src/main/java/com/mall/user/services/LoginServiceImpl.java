package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.google.errorprone.annotations.Var;
import com.mall.user.ILoginService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.UserConverterMapper;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import sun.management.snmp.jvmmib.EnumJvmJITCompilerTimeMonitoring;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

@Service
@Component
@Slf4j
public class LoginServiceImpl implements ILoginService {
    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserConverterMapper userConverterMapper;

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        UserLoginResponse loginResponse = new UserLoginResponse();
        //验证用户名和密码

        //判空验证
        request.requestCheck();

        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("username",request.getUserName());
        List<Member> members = memberMapper.selectByExample(example);
        //在数据库中没查该用户
        if(CollectionUtils.isEmpty(members)){
            loginResponse.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            loginResponse.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return loginResponse;
        }
        Member member = members.get(0);
        String password = request.getPassword();
        String md5password = DigestUtils.md5DigestAsHex(password.getBytes());
        if(!md5password.equals(member.getPassword())){
            loginResponse.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            loginResponse.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return loginResponse;
        }

        //产生jwt token
        HashMap<String, Object> map = new HashMap<String,Object>();
        map.put("uid",member.getId());
        map.put("file",member.getFile());
        map.put("username",member.getUsername());
        String token = JwtTokenUtils.builder().msg(JSON.toJSONString(map)).build().creatJwtToken();

        loginResponse = userConverterMapper.converter(member);
        loginResponse.setToken(token);
        loginResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        loginResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return loginResponse;
    }

    //验证token
    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) {
        CheckAuthResponse checkAuthResponse = new CheckAuthResponse();
        checkAuthRequest.requestCheck();
        String freeJwt = JwtTokenUtils.builder().token(checkAuthRequest.getToken()).build().freeJwt();
        if(StringUtils.isEmpty(freeJwt)){
            checkAuthResponse.setMsg(SysRetCodeConstants.TOKEN_VALID_FAILED.getMessage());
            checkAuthResponse.setCode(SysRetCodeConstants.TOKEN_VALID_FAILED.getCode());
            return checkAuthResponse;
        }
        checkAuthResponse.setUserinfo(freeJwt);
        checkAuthResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        checkAuthResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        return checkAuthResponse;
    }
}
