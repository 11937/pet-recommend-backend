package com.pet.user.service.Impl.elepet;

import com.pet.user.entity.elepet.UserCreativePoints;
import com.pet.user.mapper.elepet.UserCreativePointsMapper;
import com.pet.user.service.elepet.CreativePointsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class CreativePointsServiceImpl implements CreativePointsService {

    @Resource
    private UserCreativePointsMapper pointsMapper;

    @Override
    public int getPoints(Long userId) {
        UserCreativePoints points = pointsMapper.selectById(userId);
        return points == null ? 0 : points.getPoints();
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points) {
        UserCreativePoints userPoints = pointsMapper.selectById(userId);
        if (userPoints == null) {
            userPoints = new UserCreativePoints();
            userPoints.setUserId(userId);
            userPoints.setPoints(points);
            pointsMapper.insert(userPoints);
        } else {
            userPoints.setPoints(userPoints.getPoints() + points);
            pointsMapper.updateById(userPoints);
        }
    }

    @Override
    @Transactional
    public void deductPoints(Long userId, int points) {
        UserCreativePoints userPoints = pointsMapper.selectById(userId);
        if (userPoints == null || userPoints.getPoints() < points) {
            throw new RuntimeException("点数不足");
        }
        userPoints.setPoints(userPoints.getPoints() - points);
        pointsMapper.updateById(userPoints);
    }
}