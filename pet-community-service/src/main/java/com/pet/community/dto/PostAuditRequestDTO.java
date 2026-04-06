package com.pet.community.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 预留：审核决策入参，当前接口不落库。 */
@Data
public class PostAuditRequestDTO {

    /**
     * 预留：APPROVE / REJECT（实际审核流水线接入前仅作占位）。
     */
    @NotBlank
    private String decision;

    private String remark;
}
