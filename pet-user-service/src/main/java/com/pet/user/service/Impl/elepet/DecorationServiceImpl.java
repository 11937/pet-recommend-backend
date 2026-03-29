package com.pet.user.service.Impl.elepet;


import com.pet.user.dto.elepet.DecorationBuyDTO;
import com.pet.user.entity.elepet.DecorationItem;
import com.pet.user.entity.elepet.UserCreativePoints;
import com.pet.user.entity.elepet.UserDecoration;
import com.pet.user.mapper.elepet.DecorationItemMapper;
import com.pet.user.mapper.elepet.UserCreativePointsMapper;
import com.pet.user.mapper.elepet.UserDecorationMapper;
import com.pet.user.service.elepet.DecorationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;

@Service
public class DecorationServiceImpl implements DecorationService {

    @Resource
    private UserCreativePointsMapper userCreativePointsMapper;

    @Resource
    private UserDecorationMapper userDecorationMapper;

    @Resource
    private DecorationItemMapper decorationItemMapper;

    @Override
    public int getUserPoints(Long userId) {
        UserCreativePoints points = userCreativePointsMapper.selectById(userId);
        return points != null ? points.getPoints() : 0;
    }

    @Override
    public Map<String, Set<String>> getUserDecorations(Long userId) {
        List<UserDecoration> list = userDecorationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserDecoration>()
                        .eq(UserDecoration::getUserId, userId)
        );

        Map<String, Set<String>> result = new HashMap<>();
        for (UserDecoration dec : list) {
            result.computeIfAbsent(dec.getDecorationType(), k -> new HashSet<>())
                    .add(dec.getItemId());
        }
        return result;
    }

    @Override
    @Transactional
    public void buyDecoration(Long userId, DecorationBuyDTO dto) {
        // 1. 查询装饰品是否存在
        DecorationItem item = decorationItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DecorationItem>()
                        .eq(DecorationItem::getType, dto.getType())
                        .eq(DecorationItem::getItemId, dto.getItemId())
        );
        if (item == null) {
            throw new RuntimeException("装饰品不存在");
        }

        // 2. 检查用户点数是否足够
        int currentPoints = getUserPoints(userId);
        if (currentPoints < item.getCostPoints()) {
            throw new RuntimeException("点数不足");
        }

        // 3. 检查是否已拥有
        long count = userDecorationMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserDecoration>()
                        .eq(UserDecoration::getUserId, userId)
                        .eq(UserDecoration::getDecorationType, dto.getType())
                        .eq(UserDecoration::getItemId, dto.getItemId())
        );
        if (count > 0) {
            throw new RuntimeException("已拥有该装饰品");
        }

        // 4. 扣除点数
        userCreativePointsMapper.deductPoints(userId, item.getCostPoints());

        // 5. 记录已拥有
        UserDecoration userDec = new UserDecoration();
        userDec.setUserId(userId);
        userDec.setDecorationType(dto.getType());
        userDec.setItemId(dto.getItemId());
        userDec.setCreatedAt(java.time.LocalDateTime.now());
        userDecorationMapper.insert(userDec);
    }
}