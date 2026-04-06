package com.pet.user.service;

import com.pet.common.vo.PageSliceVO;
import com.pet.user.dto.DmSendDTO;
import com.pet.user.vo.DmMessageVO;

public interface UserDmService {

    DmMessageVO send(long senderId, DmSendDTO dto);

    PageSliceVO<DmMessageVO> pageConversation(long viewerId, long otherUserId, long page, long size);

    void markConversationRead(long viewerId, long otherUserId);

    long unreadCount(long receiverId);
}
