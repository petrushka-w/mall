package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.IRegisterService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import jodd.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class RegisterServiceImpl implements IRegisterService {
    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserVerifyMapper userVerifyMapper;

    @Override
    public UserRegisterResponse register(UserRegisterRequest registerRequest) {
        UserRegisterResponse response = new UserRegisterResponse();

        //判空验证
        registerRequest.requestCheck();

        //验证用户名是否重复
        validUserNameRepeat(registerRequest);

       //1.向用户表中插入一条记录
        Member member = new Member();
        member.setUsername(registerRequest.getUserName());
        member.setEmail(registerRequest.getEmail());
        //password加密处理
        String userpassword = DigestUtils.md5DigestAsHex(registerRequest.getUserPwd().getBytes());
        member.setPassword(userpassword);

        //设置默认值
        member.setCreated(new Date());
        member.setUpdated(new Date());
        member.setIsVerified("N");
        member.setState(1);

        int effectrows1 = memberMapper.insert(member);

        //插入不成功
        if(effectrows1!=1){
            response.setCode(SysRetCodeConstants.USER_REGISTER_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.USER_REGISTER_FAILED.getMessage());
            return response;
        }

        //2.插入成功，再向用户验证表插入记录
        UserVerify userVerify = new UserVerify();
        userVerify.setUsername(registerRequest.getUserName());
        String key = member.getUsername() + member.getPassword() + UUID.randomUUID().toString();
        String uuid=DigestUtils.md5DigestAsHex(key.getBytes());
        userVerify.setUuid(uuid);
        userVerify.setRegisterDate(new Date());
        userVerify.setIsVerify("N");
        userVerify.setIsExpire("N");

        int effectrows2 = userVerifyMapper.insert(userVerify);

        //由于验证失败插入不成功
        if(effectrows2!=1){
            response.setCode(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getMessage());
            return response;
        }

        //3.发送用户激活邮件
        //TODO

        //打印日志
        log.info("用户注册成功，注册参数request:{}", JSON.toJSONString(registerRequest.toString()));
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return response;

    }

    private void validUserNameRepeat(UserRegisterRequest registerRequest) {
        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("username",registerRequest.getUserName());
        List<Member> members = memberMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(members)){
            throw new ValidateException(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getCode(),SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getMessage());
        }
    }
}
