package com.lyh.codefather.service;

import com.mybatisflex.core.service.IService;
import com.lyh.codefather.model.entity.ChatHistory;

/**
 * 对话历史 服务层。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since ${DATE} $TIME
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);
}
