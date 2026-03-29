package com.pet.user.service.elepet;

import com.pet.user.dto.elepet.HealthRecordCreateDTO;
import com.pet.user.entity.elepet.HealthRecord;
import com.pet.user.vo.elepet.HealthRecordVO;
import java.time.LocalDate;
import java.util.List;

public interface HealthRecordService {
    HealthRecordVO addRecord(Long userId, HealthRecordCreateDTO dto);
    List<HealthRecordVO> listByPetId(Long petId, LocalDate startDate, LocalDate endDate);
    List<HealthRecord> listByPetIdAndTypeAndDateRange(Long petId, Integer recordType, LocalDate startDate, LocalDate endDate);
}