package com.lyh.codefather.ai.core;

import com.lyh.codefather.ai.AiCodeGeneratorService;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import com.lyh.codefather.exception.BusinessException;
import com.lyh.codefather.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI代码生成器门面类 - 集成流式输出解析功能
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 2.0
 * @since 2025/11/13 23:00
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据生成类型生成代码并保存
     * @param userMessage 用户输入
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum){
            case HTML -> generateAndSaveSingleFileCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
        };
    }

    /**
     * 处理流式输出结果并保存
     * @param streamResponse 流式响应内容
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File processStreamAndSave(String streamResponse, CodeGenTypeEnum codeGenTypeEnum) {
        log.info("开始处理流式输出结果，类型: {}", codeGenTypeEnum);
        
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        
        // 验证流式响应内容
        if (!StreamCodeProcessor.isValidStreamResponse(streamResponse)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的流式响应内容");
        }
        
        // 使用流式处理器处理
        return StreamCodeProcessor.processStreamByType(streamResponse, codeGenTypeEnum);
    }

    /**
     * 生成多文件代码并保存
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiHtmlFileCodeResult result = aiCodeGeneratorService.generateMultipleFiles(userMessage);
        return CodeFileSaver.saveMultiHtmlCodeResult(result);
    }

    /**
     * 生成单文件代码并保存
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveSingleFileCode(String userMessage) {
        SingleHtmlFileCodeResult result = aiCodeGeneratorService.generateSingleFile(userMessage);
        return CodeFileSaver.saveSingleHtmlCodeResult(result);
    }
}
