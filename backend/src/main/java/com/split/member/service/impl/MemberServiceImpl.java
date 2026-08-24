package com.split.member.service.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.split.common.exception.BadRequestException;
import com.split.common.exception.ConflictException;
import com.split.common.exception.NotFoundException;
import com.split.common.web.ApiErrorCode;
import com.split.ledger.mapper.LedgerMapper;
import com.split.member.dto.MemberSaveDTO;
import com.split.member.entity.Member;
import com.split.member.mapper.MemberMapper;
import com.split.member.service.MemberService;
import com.split.member.vo.MemberVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final LedgerMapper ledgerMapper;

    /**
     * 根据账本 ID 获取成员列表
     * 
     * @param ledgerId 账本主键
     * @return 成员视图对象列表
     */
    @Override
    public List<MemberVO> getMembersByLedgerId(Long ledgerId) {
        if (ledgerId == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "账本 ID 不能为空");
        }

        // 构造查询条件：指定归属的账本，并规定“先加进来的成员排前面”
        LambdaQueryWrapper<Member> query = new LambdaQueryWrapper<>();
        query.eq(Member::getLedgerId, ledgerId)
                .orderByDesc(Member::getCreatedAt);

        return memberMapper.selectList(query)
                .stream()
                .map(MemberVO::fromEntity)
                .toList();

    }

    /**
     * 在指定账本下添加新成员
     * 
     * @param ledgerId 账本主键
     * @param dto      成员数据
     * @return 新成员 ID
     */
    @Override
    public Long createMember(Long ledgerId, MemberSaveDTO dto) {
        if (ledgerId == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "账本 ID 不能为空");
        }
        if (dto == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "成员数据不能为空");
        }

        // 兜底校验：防止前端传了一个根本不存在的 ledgerId
        if (ledgerMapper.selectById(ledgerId) == null) {
            throw new NotFoundException(ApiErrorCode.LEDGER_NOT_FOUND, "账本不存在");
        }

        // 防重校验：严禁同账本下同名
        LambdaQueryWrapper<Member> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Member::getLedgerId, ledgerId)
                .eq(Member::getName, dto.getName());

        if (memberMapper.selectCount(existQuery) > 0) {
            throw new ConflictException(ApiErrorCode.MEMBER_NAME_CONFLICT, "成员名称已存在");
        }

        // 校验通过，执行插入
        Member member = new Member(ledgerId, dto.getName());
        memberMapper.insert(member);

        return member.getId();
    }

    /**
     * 修改成员姓名
     *
     * @param ledgerId 账本主键
     * @param memberId 成员主键
     * @param dto      包含新名称的数据
     * @return 修改后的成员视图对象
     */
    @Override
    public MemberVO updateMember(Long ledgerId, Long memberId, MemberSaveDTO dto) {
        if (ledgerId == null || memberId == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "账本 ID 或成员 ID 不能为空");
        }

        // 确认该成员存在，且它真的是这个账本的（防止前端伪造路由越权）
        Member member = memberMapper.selectById(memberId);
        if (member == null || !member.getLedgerId().equals(ledgerId)) {
            throw new NotFoundException(ApiErrorCode.MEMBER_NOT_FOUND, "成员不存在或不属于该账本");
        }

        if (dto == null || member.getName().equals(dto.getName())) {
            return MemberVO.fromEntity(member);
        }

        // 查重逻辑：同一个账本下，有没有其他人叫这个新名字？
        LambdaQueryWrapper<Member> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Member::getLedgerId, ledgerId)
                .eq(Member::getName, dto.getName())
                .ne(Member::getId, memberId);

        if (memberMapper.selectCount(existQuery) > 0) {
            throw new ConflictException(ApiErrorCode.MEMBER_NAME_CONFLICT, "该账本下已存在同名成员");
        }

        // 更新并保存
        member.setName(dto.getName());
        memberMapper.updateById(member);

        return MemberVO.fromEntity(member);
    }

    /**
     * 删除成员
     *
     * @param ledgerId 账本主键
     * @param memberId 成员主键
     */
    @Override
    @Transactional
    public void deleteMember(Long ledgerId, Long memberId) {
        if (ledgerId == null || memberId == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "账本 ID 或成员 ID 不能为空");
        }

        // 确认该成员存在，且属于当前账本
        Member member = memberMapper.selectById(memberId);
        if (member == null || !member.getLedgerId().equals(ledgerId)) {
            throw new NotFoundException(ApiErrorCode.MEMBER_NOT_FOUND, "成员不存在或不属于该账本");
        }

        // 尝试删除
        try {
            memberMapper.deleteById(memberId);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ApiErrorCode.MEMBER_IN_USE, "该成员已有相关账单消费记录，无法删除");
        }
    }
}
