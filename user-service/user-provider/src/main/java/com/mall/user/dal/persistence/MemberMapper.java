package com.mall.user.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.user.dal.entitys.Member;
import org.springframework.stereotype.Component;

@Component
public interface MemberMapper extends TkMapper<Member> {
}