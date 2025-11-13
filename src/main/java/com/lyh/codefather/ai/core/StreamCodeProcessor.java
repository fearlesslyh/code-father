package com.lyh.codefather.ai.core;

import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 流式代码处理器 - 集成CodeParser和CodeFileSaver
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/14 00:15
 */
@Slf4j
public class StreamCodeProcessor {

    /**
     * 处理单文件代码的流式输出
     *
     * @param streamResponse 流式响应内容
     * @return 保存的文件目录
     */
    public static File processSingleFileStream(String streamResponse) {
        log.info("开始处理单文件流式输出");
        
        // 1. 解析流式输出
        SingleHtmlFileCodeResult result = CodeParser.parseSingleFileStream(streamResponse);
        
        // 2. 保存文件
        File savedDir = CodeFileSaver.saveSingleHtmlCodeResult(result);
        
        log.info("单文件代码处理完成，保存目录: {}", savedDir.getAbsolutePath());
        return savedDir;
    }

    /**
     * 处理多文件代码的流式输出
     *
     * @param streamResponse 流式响应内容
     * @return 保存的文件目录
     */
    public static File processMultiFileStream(String streamResponse) {
        log.info("开始处理多文件流式输出");
        
        // 1. 解析流式输出
        MultiHtmlFileCodeResult result = CodeParser.parseMultiFileStream(streamResponse);
        
        // 2. 保存文件
        File savedDir = CodeFileSaver.saveMultiHtmlCodeResult(result);
        
        log.info("多文件代码处理完成，保存目录: {}", savedDir.getAbsolutePath());
        return savedDir;
    }

    /**
     * 根据代码生成类型处理流式输出
     *
     * @param streamResponse 流式响应内容
     * @param codeGenType 代码生成类型
     * @return 保存的文件目录
     */
    public static File processStreamByType(String streamResponse, CodeGenTypeEnum codeGenType) {
        log.info("根据类型处理流式输出: {}", codeGenType);
        
        switch (codeGenType) {
            case HTML:
                return processSingleFileStream(streamResponse);
            case MULTI_FILE:
                return processMultiFileStream(streamResponse);
            default:
                log.warn("未知的代码生成类型: {}", codeGenType);
                throw new IllegalArgumentException("不支持的代码生成类型: " + codeGenType);
        }
    }

    /**
     * 验证流式输出内容是否有效
     *
     * @param streamResponse 流式响应内容
     * @return 是否有效
     */
    public static boolean isValidStreamResponse(String streamResponse) {
        if (streamResponse == null || streamResponse.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含JSON格式内容
        return streamResponse.contains("{") && streamResponse.contains("}");
    }

    /**
     * 获取流式响应的预览信息
     *
     * @param streamResponse 流式响应内容
     * @return 预览信息
     */
    public static String getPreviewInfo(String streamResponse) {
        if (!isValidStreamResponse(streamResponse)) {
            return "无效的流式响应";
        }
        
        try {
            // 提取JSON内容进行预览
            String jsonContent = extractJsonContentForPreview(streamResponse);
            if (jsonContent.length() > 200) {
                return jsonContent.substring(0, 200) + "...";
            }
            return jsonContent;
        } catch (Exception e) {
            return "预览解析失败: " + e.getMessage();
        }
    }

    /**
     * 提取JSON内容用于预览
     *
     * @param streamResponse 流式响应内容
     * @return 预览内容
     */
    private static String extractJsonContentForPreview(String streamResponse) {
        // 简单的JSON提取逻辑
        int start = streamResponse.indexOf("{");
        int end = streamResponse.lastIndexOf("}") + 1;
        
        if (start >= 0 && end > start) {
            return streamResponse.substring(start, end);
        }
        
        return streamResponse;
    }
}