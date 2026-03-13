package com.pet.cat.info.service.impl;

import com.pet.cat.info.mapper.AllCatsMapper;
import com.pet.cat.info.service.AllCatInfoService;
import com.pet.common.dto.Cat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AllCatInfoImpl implements AllCatInfoService {
    @Resource
    private AllCatsMapper allCatsMapper;

    @Override
    public List<Cat> findAllCatList() {
        return allCatsMapper.findAllCatList();
    }
}
