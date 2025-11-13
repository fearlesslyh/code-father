package com.lyh.codefather.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/13 16:47
 */

/**
 * AI代码生成器工厂
 * 要保持一种设计模式的思考习惯，
 * 通过工厂模式自动配置负责创建 Assistant bean。使用IOC容器管理bean。
 * 这意味着你不需要调用 AiServices.create(...) ，你只需在需要的地方注入/自动装配 AiCodeGeneratorService 即可。
 */
@Configuration
public class AiCodeGeneratorFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel streamingChatModel;

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
