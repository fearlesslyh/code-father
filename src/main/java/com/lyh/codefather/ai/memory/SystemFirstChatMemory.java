package com.lyh.codefather.ai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 ChatMemory，确保 system 消息始终在消息列表的最前面
 * 用于解决腾讯混元 API 要求 system 消息必须在最前面的问题
 * 使用组合模式包装 MessageWindowChatMemory
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 */
@Data
public class SystemFirstChatMemory implements ChatMemory {

    private final MessageWindowChatMemory delegate;

    private SystemFirstChatMemory(MessageWindowChatMemory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object id() {
        return delegate.id();
    }

    @Override
    public void add(ChatMessage message) {
        delegate.add(message);
    }

    @Override
    public List<ChatMessage> messages() {
        List<ChatMessage> allMessages = delegate.messages();
        if (allMessages.isEmpty()) {
            return allMessages;
        }

        // 分离 system 消息和其他消息
        List<ChatMessage> systemMessages = new ArrayList<>();
        List<ChatMessage> otherMessages = new ArrayList<>();

        for (ChatMessage message : allMessages) {
            if (message instanceof SystemMessage) {
                systemMessages.add(message);
            } else {
                otherMessages.add(message);
            }
        }

        // 将 system 消息放在最前面
        List<ChatMessage> result = new ArrayList<>();
        result.addAll(systemMessages);
        result.addAll(otherMessages);

        return result;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private Object id;
        private ChatMemoryStore chatMemoryStore;
        private int maxMessages = 20;

        public SystemFirstChatMemory build() {
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder();
            if (id != null) {
                builder.id(id);
            }
            if (chatMemoryStore != null) {
                builder.chatMemoryStore(chatMemoryStore);
            }
            builder.maxMessages(maxMessages);
            MessageWindowChatMemory delegate = builder.build();
            return new SystemFirstChatMemory(delegate);
        }
    }
}

