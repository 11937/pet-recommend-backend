package com.pet.user.dto.elepet;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HealthRecordCreateDTO {
    private Long petId;
    private Integer recordType; // 1-体重 2-喂食 3-蜕皮 4-排便 5-其他
    private String value;
    private String unit;
    private String remark;
    private LocalDate recordDate; // 可选，默认为当天
}