package com.lyh.codefather.service;

import com.lyh.codefather.model.dto.chathistory.ChatHistoryQueryRequest;
import com.lyh.codefather.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lyh.codefather.model.entity.ChatHistory;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since ${DATE} $TIME
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);
}
