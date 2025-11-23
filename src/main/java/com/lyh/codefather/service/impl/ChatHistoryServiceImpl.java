package com.lyh.codefather.service.impl;

import com.lyh.codefather.exception.ErrorCode;
import com.lyh.codefather.exception.ThrowUtils;
import com.lyh.codefather.model.enums.ChatHistoryMessageTypeEnum;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lyh.codefather.model.entity.ChatHistory;
import com.lyh.codefather.mapper.ChatHistoryMapper;
import com.lyh.codefather.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since ${DATE} $TIME
 */

/**
 * 对话历史 服务层实现。
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId){
        ThrowUtils.throwIf(appId == null||appId<=0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        ThrowUtils.throwIf(message == null||message.isEmpty(), ErrorCode.PARAMS_ERROR,"消息不能为空");
        ThrowUtils.throwIf(messageType == null||messageType.isEmpty(), ErrorCode.PARAMS_ERROR,"消息类型不能为空");
        ThrowUtils.throwIf(userId == null||userId<=0, ErrorCode.PARAMS_ERROR,"用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR,"消息类型无效"+messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageTypeEnum.getValue())
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }
    @Override
    public boolean deleteByAppId(Long appId){
        ThrowUtils.throwIf(appId == null||appId<=0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }
}
