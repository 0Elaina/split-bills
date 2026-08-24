package com.split.member.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.split.common.web.Result;
import com.split.member.dto.MemberSaveDTO;
import com.split.member.service.MemberService;
import com.split.member.vo.MemberVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 获取账本下的所有成员
     * 
     * @param ledgerId 账本主键
     * @return 成员视图对象列表
     */
    @GetMapping
    public Result<List<MemberVO>> getMembers(
            @PathVariable("ledgerId") Long ledgerId) {
        return Result.success(memberService.getMembersByLedgerId(ledgerId));
    }

    /**
     * 在指定账本下添加新成员
     *
     * @param ledgerId 路径中的账本 ID
     * @param dto      前端传入的成员数据（受 @Validated 保护）
     * @return 新成员 ID（转为 String 防止 JS 精度丢失）
     */
    @PostMapping
    public Result<String> createMember(
            @PathVariable("ledgerId") Long ledgerId,
            @RequestBody @Validated MemberSaveDTO dto) {
        return Result.success(
                String.valueOf(memberService.createMember(ledgerId, dto)));
    }

    /**
     * 修改成员名称
     *
     * @param ledgerId 账本 ID
     * @param memberId 成员 ID
     * @param dto      包含新名称的请求体（受 @Validated 保护）
     * @return 修改后的成员信息
     */
    @PatchMapping("/{memberId}")
    public Result<MemberVO> updateMember(
            @PathVariable("ledgerId") Long ledgerId,
            @PathVariable("memberId") Long memberId,
            @RequestBody @Validated MemberSaveDTO dto) {
        return Result.success(memberService.updateMember(ledgerId, memberId, dto));
    }

    /**
     * 删除成员
     *
     * @param ledgerId 账本 ID
     * @param memberId 成员 ID
     * @return 空的成功响应
     */
    @DeleteMapping("/{memberId}")
    public Result<Void> deleteMember(
            @PathVariable("ledgerId") Long ledgerId,
            @PathVariable("memberId") Long memberId) {
        memberService.deleteMember(ledgerId, memberId);
        return Result.success();
    }
}
