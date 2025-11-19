package com.lyh.codefather.ai.core.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML单文件代码解析器
 * 实现策略模式，专门处理单文件代码的解析
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
public class SingleFileCodeParser implements CodeParser<SingleHtmlFileCodeResult> {

    // JSON格式的正则表达式模式
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    
    // 代码块的正则表达式模式
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    @Override
    public SingleHtmlFileCodeResult parseStream(String streamResponse) {
        log.info("开始解析单文件流式输出结果");
        
        // 1. 提取JSON内容
        String jsonContent = extractJsonContent(streamResponse);
        if (StrUtil.isBlank(jsonContent)) {
            log.warn("未找到有效的JSON内容");
            return createDefaultSingleFileResult();
        }

        // 2. 解析JSON
        try {
            JSONObject jsonObject = JSONUtil.parseObj(jsonContent);
            SingleHtmlFileCodeResult result = new SingleHtmlFileCodeResult();
            
            // 3. 设置HTML代码
            String htmlCode = jsonObject.getStr("htmlCode");
            if (StrUtil.isNotBlank(htmlCode)) {
                result.setHtmlCode(htmlCode);
            } else {
                log.warn("JSON中未找到htmlCode字段");
                result.setHtmlCode("<!DOCTYPE html><html><head><title>默认页面</title></head><body><h1>代码生成失败</h1></body></html>");
            }
            
            // 4. 设置CSS代码（如果存在）
            String cssCode = jsonObject.getStr("cssCode");
            if (StrUtil.isNotBlank(cssCode)) {
                result.setCssCode(cssCode);
            }
            
            log.info("单文件代码解析成功");
            return result;
            
        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage(), e);
            return createDefaultSingleFileResult();
        }
    }

    @Override
    public String getSupportedType() {
        return "html";
    }

    /**
     * 从流式响应中提取JSON内容
     *
     * @param streamResponse 流式响应内容
     * @return 提取的JSON内容
     */
    private String extractJsonContent(String streamResponse) {
        if (StrUtil.isBlank(streamResponse)) {
            return "";
        }
        
        // 首先尝试提取代码块中的JSON
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(streamResponse);
        if (codeBlockMatcher.find()) {
            String content = codeBlockMatcher.group(1);
            log.debug("从代码块中提取到内容: {}", content);
            return content;
        }
        
        // 如果没有代码块，直接查找JSON对象
        Matcher jsonMatcher = JSON_PATTERN.matcher(streamResponse);
        if (jsonMatcher.find()) {
            String jsonContent = jsonMatcher.group();
            log.debug("直接提取到JSON内容: {}", jsonContent);
            return jsonContent;
        }
        
        // 如果都没有找到，返回原始内容
        return streamResponse;
    }

    /**
     * 创建默认的单文件结果
     *
     * @return 默认结果
     */
    private SingleHtmlFileCodeResult createDefaultSingleFileResult() {
        SingleHtmlFileCodeResult result = new SingleHtmlFileCodeResult();
        result.setHtmlCode("<!DOCTYPE html><html><head><title>默认页面</title></head><body><h1>代码生成失败</h1></body></html>");
        result.setCssCode("/* 默认样式 */\nbody { margin: 0; padding: 20px; font-family: Arial, sans-serif; }");
        return result;
    }
}