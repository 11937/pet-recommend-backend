package com.pet.user.vo.elepet;

import com.pet.user.entity.elepet.HealthRecord;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthRecordVO {
    private Long id;
    private Long petId;
    private Integer recordType;
    private String value;
    private String unit;
    private String remark;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private Integer currentPoints; // 可选，返回最新点数

    public static HealthRecordVO from(HealthRecord record) {
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(record.getId());
        vo.setPetId(record.getPetId());
        vo.setRecordType(record.getRecordType());
        vo.setValue(record.getValue());
        vo.setUnit(record.getUnit());
        vo.setRemark(record.getRemark());
        vo.setRecordDate(record.getRecordDate());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
