package com.pet.user.service.Impl.elepet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.user.dto.elepet.HealthRecordCreateDTO;
import com.pet.user.entity.elepet.HealthRecord;
import com.pet.user.entity.elepet.PetProfile;
import com.pet.user.mapper.elepet.HealthRecordMapper;
import com.pet.user.mapper.elepet.PetProfileMapper;
import com.pet.user.service.elepet.HealthRecordService;
import com.pet.user.vo.elepet.HealthRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    @Resource
    private HealthRecordMapper healthRecordMapper;
    @Resource
    private PetProfileMapper petProfileMapper;

    @Override
    @Transactional
    public HealthRecordVO addRecord(Long userId, HealthRecordCreateDTO dto) {
        // 1. 校验宠物归属
        PetProfile pet = petProfileMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new RuntimeException("宠物不存在或无权操作");
        }

        // 2. 保存记录
        HealthRecord record = new HealthRecord();
        record.setPetId(dto.getPetId());
        record.setRecordType(dto.getRecordType());
        record.setValue(dto.getValue());
        record.setUnit(dto.getUnit());
        record.setRemark(dto.getRemark());
        record.setRecordDate(dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now());
        healthRecordMapper.insert(record);

        // 3. 暂时不处理点数，后续再加
        return HealthRecordVO.from(record);
    }

    @Override
    public List<HealthRecordVO> listByPetId(Long petId, LocalDate startDate, LocalDate endDate) {
        // 可根据宠物ID和日期范围查询，此处先简单返回全部
        List<HealthRecord> records = healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getPetId, petId)
                        .ge(startDate != null, HealthRecord::getRecordDate, startDate)
                        .le(endDate != null, HealthRecord::getRecordDate, endDate)
                        .orderByDesc(HealthRecord::getRecordDate)
        );
        return records.stream().map(HealthRecordVO::from).collect(Collectors.toList());
    }

    @Override
    public List<HealthRecord> listByPetIdAndTypeAndDateRange(Long petId, Integer recordType, LocalDate startDate, LocalDate endDate) {
        return healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getPetId, petId)
                        .eq(HealthRecord::getRecordType, recordType)
                        .ge(HealthRecord::getRecordDate, startDate)
                        .le(HealthRecord::getRecordDate, endDate)
                        .orderByAsc(HealthRecord::getRecordDate)
        );
    }
}
