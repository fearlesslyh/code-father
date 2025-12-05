package com.lyh.codefather.ai.core.handler;

import com.lyh.codefather.ai.core.builder.VueProjectBuilder;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import com.lyh.codefather.constant.AppConstant;
import com.lyh.codefather.model.entity.User;
import com.lyh.codefather.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 1. 传统的 Flux<String> 流（HTML、MULTI_FILE） -> SimpleTextStreamHandler
 * 2. Vue 项目（VUE_PROJECT） -> SimpleTextStreamHandler + VueProjectBuilder 异步构建
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML, MULTI_FILE ->
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
            case VUE_PROJECT -> {
                // 1. 先用简单文本处理器统一收集 AI 回复并写入对话历史
                Flux<String> baseFlux = new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
                // 2. 在流结束后，异步触发 Vue 项目构建（基于工具已经写好的文件）
                String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                yield baseFlux.doOnComplete(() -> vueProjectBuilder.buildProjectAsync(projectPath));
            }
        };
    }
}
