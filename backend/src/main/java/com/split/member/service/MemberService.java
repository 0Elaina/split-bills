package com.split.member.service;

import java.util.List;

import com.split.member.dto.MemberSaveDTO;
import com.split.member.vo.MemberVO;

public interface MemberService {
    /**
     * 根据账本 ID 获取成员列表
     * 
     * @param ledgerId 账本主键
     * @return 成员视图对象列表
     */
    List<MemberVO> getMembersByLedgerId(Long ledgerId);

    /**
     * 在指定账本下创建新成员
     * 
     * @param ledgerId 账本主键
     * @param dto 包含新建数据的请求体
     * @return 新创建的成员 ID
     */
    Long createMember(Long ledgerId, MemberSaveDTO dto);
}
