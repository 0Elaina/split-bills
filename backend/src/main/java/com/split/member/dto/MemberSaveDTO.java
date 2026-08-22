package com.split.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberSaveDTO {
    @NotBlank(message = "成员名称不能为空")
    @Size(max = 20, message = "成员名称最多 20 个字符")
    private String name;
}
