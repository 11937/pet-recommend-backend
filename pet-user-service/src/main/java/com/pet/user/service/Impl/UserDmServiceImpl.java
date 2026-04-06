package com.pet.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.dto.NotificationPushDTO;
import com.pet.common.vo.PageSliceVO;
import com.pet.user.dto.DmSendDTO;
import com.pet.user.entity.DmMessage;
import com.pet.user.entity.User;
import com.pet.user.mapper.DmMessageMapper;
import com.pet.user.mapper.UserMapper;
import com.pet.user.service.UserDmService;
import com.pet.user.service.UserNotificationService;
import com.pet.user.vo.DmMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDmServiceImpl implements UserDmService {

    private final DmMessageMapper dmMapper;
    private final UserMapper userMapper;
    private final UserNotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DmMessageVO send(long senderId, DmSendDTO dto) {
        if (senderId == dto.getReceiverId()) {
            throw new IllegalArgumentException("不能给自己发私信");
        }
        User recv = userMapper.selectById(dto.getReceiverId());
        if (recv == null) {
            throw new IllegalArgumentException("接收者不存在");
        }
        String text = dto.getContent().trim();
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (text.length() > 2000) {
            throw new IllegalArgumentException("内容过长");
        }
        DmMessage m = new DmMessage();
        m.setSenderId(senderId);
        m.setReceiverId(dto.getReceiverId());
        m.setContent(text);
        m.setCreatedAt(LocalDateTime.now());
        dmMapper.insert(m);
        DmMessage loaded = dmMapper.selectById(m.getId());

        User sender = userMapper.selectById(senderId);
        String name = displayName(sender, senderId);
        NotificationPushDTO n = new NotificationPushDTO();
        n.setUserId(dto.getReceiverId());
        n.setType("DM");
        n.setTitle("新私信");
        n.setBody(name + " 发来一条私信");
        n.setRefType("USER");
        n.setRefId(senderId);
        notificationService.push(n);

        return toVo(loaded != null ? loaded : m);
    }

    @Override
    public PageSliceVO<DmMessageVO> pageConversation(long viewerId, long otherUserId, long page, long size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        LambdaQueryWrapper<DmMessage> q = new LambdaQueryWrapper<DmMessage>()
                .and(w -> w
                        .eq(DmMessage::getSenderId, viewerId).eq(DmMessage::getReceiverId, otherUserId)
                        .or()
                        .eq(DmMessage::getSenderId, otherUserId).eq(DmMessage::getReceiverId, viewerId))
                .orderByDesc(DmMessage::getCreatedAt);
        Page<DmMessage> mp = dmMapper.selectPage(new Page<>(page, size), q);
        PageSliceVO<DmMessageVO> vo = new PageSliceVO<>();
        vo.setPage(mp.getCurrent());
        vo.setSize(mp.getSize());
        vo.setTotal(mp.getTotal());
        vo.setHasMore(mp.getCurrent() * mp.getSize() < mp.getTotal());
        vo.setRecords(mp.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationRead(long viewerId, long otherUserId) {
        dmMapper.update(null, new LambdaUpdateWrapper<DmMessage>()
                .eq(DmMessage::getReceiverId, viewerId)
                .eq(DmMessage::getSenderId, otherUserId)
                .isNull(DmMessage::getReadAt)
                .set(DmMessage::getReadAt, LocalDateTime.now()));
    }

    @Override
    public long unreadCount(long receiverId) {
        Integer c = dmMapper.selectCount(new LambdaQueryWrapper<DmMessage>()
                .eq(DmMessage::getReceiverId, receiverId)
                .isNull(DmMessage::getReadAt));
        return c == null ? 0L : c.longValue();
    }

    private DmMessageVO toVo(DmMessage m) {
        DmMessageVO vo = new DmMessageVO();
        vo.setId(m.getId());
        vo.setSenderId(m.getSenderId());
        vo.setReceiverId(m.getReceiverId());
        vo.setContent(m.getContent());
        vo.setCreatedAt(m.getCreatedAt());
        vo.setReadAt(m.getReadAt());
        return vo;
    }

    private String displayName(User u, long fallbackId) {
        if (u != null && StringUtils.hasText(u.getNickName())) {
            return u.getNickName();
        }
        if (u != null && StringUtils.hasText(u.getUsername())) {
            return u.getUsername();
        }
        return "用户" + fallbackId;
    }
}
