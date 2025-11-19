package com.lyh.codefather.ai.core.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器
 * 实现策略模式，专门处理多文件代码的解析
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
public class MultiFileCodeParser implements CodeParser<MultiHtmlFileCodeResult> {

    // JSON格式的正则表达式模式
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    
    // 代码块的正则表达式模式
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    @Override
    public MultiHtmlFileCodeResult parseStream(String streamResponse) {
        log.info("开始解析多文件流式输出结果");
        
        // 1. 提取JSON内容
        String jsonContent = extractJsonContent(streamResponse);
        if (StrUtil.isBlank(jsonContent)) {
            log.warn("未找到有效的JSON内容");
            return createDefaultMultiFileResult();
        }

        // 2. 解析JSON
        try {
            JSONObject jsonObject = JSONUtil.parseObj(jsonContent);
            MultiHtmlFileCodeResult result = new MultiHtmlFileCodeResult();
            
            // 3. 设置HTML代码
            String htmlCode = jsonObject.getStr("htmlCode");
            if (StrUtil.isNotBlank(htmlCode)) {
                // 确保HTML代码正确引入CSS和JS文件
                htmlCode = ensureHtmlFileReferences(htmlCode);
                result.setHtmlCode(htmlCode);
            } else {
                log.warn("JSON中未找到htmlCode字段");
                result.setHtmlCode(createDefaultHtmlWithReferences());
            }
            
            // 4. 设置CSS代码
            String cssCode = jsonObject.getStr("cssCode");
            if (StrUtil.isNotBlank(cssCode)) {
                result.setCssCode(cssCode);
            } else {
                result.setCssCode("/* 默认样式 */\nbody { margin: 0; padding: 20px; font-family: Arial, sans-serif; }");
            }
            
            // 5. 设置JS代码
            String jsCode = jsonObject.getStr("jsCode");
            if (StrUtil.isNotBlank(jsCode)) {
                result.setJsCode(jsCode);
            } else {
                result.setJsCode("// 默认JavaScript代码\nconsole.log('页面加载完成');");
            }
            
            // 6. 设置描述
            String description = jsonObject.getStr("description");
            if (StrUtil.isNotBlank(description)) {
                result.setDescription(description);
            }
            
            log.info("多文件代码解析成功");
            return result;
            
        } catch (Exception e) {
            log.error("JSON解析失败: {}", e.getMessage(), e);
            return createDefaultMultiFileResult();
        }
    }

    @Override
    public String getSupportedType() {
        return "multi_file";
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
     * 确保HTML文件正确引入CSS和JS文件
     *
     * @param htmlCode 原始HTML代码
     * @return 修正后的HTML代码
     */
    private String ensureHtmlFileReferences(String htmlCode) {
        if (StrUtil.isBlank(htmlCode)) {
            return createDefaultHtmlWithReferences();
        }
        
        // 检查是否已经包含CSS和JS引用
        boolean hasCssLink = htmlCode.contains("<link") && htmlCode.contains("style.css");
        boolean hasJsScript = htmlCode.contains("<script") && htmlCode.contains("script.js");
        
        // 如果已经包含正确的引用，直接返回
        if (hasCssLink && hasJsScript) {
            return htmlCode;
        }
        
        // 构建正确的HTML头部
        StringBuilder htmlBuilder = new StringBuilder();
        
        // 查找<head>标签位置
        int headStart = htmlCode.indexOf("<head>");
        int headEnd = htmlCode.indexOf("</head>");
        
        if (headStart != -1 && headEnd != -1) {
            // 在<head>标签内添加CSS引用
            String headContent = htmlCode.substring(headStart + 6, headEnd);
            
            // 构建新的head内容
            StringBuilder newHead = new StringBuilder();
            newHead.append("<head>");
            
            // 保留原有的head内容
            newHead.append(headContent);
            
            // 添加CSS引用（如果没有）
            if (!hasCssLink) {
                newHead.append("\n    <link rel=\"stylesheet\" href=\"style.css\">");
            }
            
            newHead.append("\n</head>");
            
            // 替换原有的head部分
            htmlBuilder.append(htmlCode.substring(0, headStart));
            htmlBuilder.append(newHead.toString());
            htmlBuilder.append(htmlCode.substring(headEnd + 7));
        } else {
            // 如果没有找到head标签，直接添加CSS引用
            htmlBuilder.append(htmlCode);
        }
        
        // 检查并添加JS引用
        int bodyEnd = htmlBuilder.indexOf("</body>");
        if (bodyEnd != -1 && !hasJsScript) {
            htmlBuilder.insert(bodyEnd, "\n    <script src=\"script.js\"></script>");
        }
        
        return htmlBuilder.toString();
    }

    /**
     * 创建默认的HTML文件，包含CSS和JS引用
     *
     * @return 默认HTML代码
     */
    private String createDefaultHtmlWithReferences() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>默认页面</title>\n" +
                "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>代码生成失败</h1>\n" +
                "    <script src=\"script.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 创建默认的多文件结果
     *
     * @return 默认结果
     */
    private MultiHtmlFileCodeResult createDefaultMultiFileResult() {
        MultiHtmlFileCodeResult result = new MultiHtmlFileCodeResult();
        result.setHtmlCode(createDefaultHtmlWithReferences());
        result.setCssCode("/* 默认样式 */\nbody { margin: 0; padding: 20px; font-family: Arial, sans-serif; }");
        result.setJsCode("// 默认JavaScript代码\nconsole.log('页面加载完成');");
        result.setDescription("默认生成的多文件代码");
        return result;
    }
}