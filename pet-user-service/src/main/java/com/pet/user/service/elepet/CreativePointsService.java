package com.pet.user.service.elepet;

public interface CreativePointsService {
    int getPoints(Long userId);
    void addPoints(Long userId, int points);
    void deductPoints(Long userId, int points);
}
