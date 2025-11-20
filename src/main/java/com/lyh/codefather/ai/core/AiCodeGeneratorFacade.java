package com.lyh.codefather.ai.core;

import com.lyh.codefather.ai.AiCodeGeneratorService;
import com.lyh.codefather.ai.core.parser.CodeParserExecutor;
import com.lyh.codefather.ai.core.saver.CodeFileSaverExecutor;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import com.lyh.codefather.exception.BusinessException;
import com.lyh.codefather.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * AI代码生成器门面类 - 优化版本
 * 使用执行器模式、策略模式和模板方法模式重构
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 3.0
 * @since 2025/11/13 23:00
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    
    @Resource
    private CodeParserExecutor codeParserExecutor;
    
    @Resource
    private CodeFileSaverExecutor codeFileSaverExecutor;

    /**
     * 统一入口：根据生成类型生成代码并保存
     * @param userMessage 用户输入
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    @Retryable(value = {SocketTimeoutException.class, ConnectException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public File generateAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 参数校验
        if (codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        log.info("开始生成代码，类型: {}, 应用ID: {}", codeGenTypeEnum.getValue(), appId);
        
        // 生成代码
        Object result = generateCodeByType(userMessage, codeGenTypeEnum);
        
        // 保存代码
        return saveCodeByType(result, codeGenTypeEnum, appId);
    }

    /**
     * 处理流式输出结果并保存
     * @param streamResponse 流式响应内容
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    @Retryable(value = {SocketTimeoutException.class, ConnectException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public File processStreamAndSave(String streamResponse, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 参数校验
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        log.info("开始处理流式输出结果，类型: {}, 应用ID: {}", codeGenTypeEnum, appId);
        
        // 验证流式响应内容
        if (streamResponse == null || streamResponse.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的流式响应内容");
        }
        
        // 检查是否包含JSON格式内容
        if (!streamResponse.contains("{") || !streamResponse.contains("}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "流式响应内容格式不正确");
        }
        
        // 解析流式输出
        Object result = parseStreamByType(streamResponse, codeGenTypeEnum);
        
        // 保存代码
        return saveCodeByType(result, codeGenTypeEnum, appId);
    }

    /**
     * 根据类型生成代码
     *
     * @param userMessage 用户输入
     * @param codeGenTypeEnum 生成类型
     * @return 生成结果
     */
    private Object generateCodeByType(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> aiCodeGeneratorService.generateSingleFile(userMessage);
            case MULTI_FILE -> aiCodeGeneratorService.generateMultipleFiles(userMessage);
        };
    }

    /**
     * 根据类型解析流式输出
     *
     * @param streamResponse 流式响应内容
     * @param codeGenTypeEnum 生成类型
     * @return 解析结果
     */
    private Object parseStreamByType(String streamResponse, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> codeParserExecutor.parseSingleFile(streamResponse);
            case MULTI_FILE -> codeParserExecutor.parseMultiFile(streamResponse);
        };
    }

    /**
     * 根据类型保存代码
     *
     * @param result 解析结果
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    private File saveCodeByType(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> codeFileSaverExecutor.saveSingleFile((SingleHtmlFileCodeResult) result, appId);
            case MULTI_FILE -> codeFileSaverExecutor.saveMultiFile((MultiHtmlFileCodeResult) result, appId);
        };
    }
}
