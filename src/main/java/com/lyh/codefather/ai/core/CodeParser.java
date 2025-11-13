package com.lyh.codefather.ai.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LangChain流式输出结果解析器
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/14 00:06
 */
@Slf4j
public class CodeParser {

    // JSON格式的正则表达式模式
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    
    // 代码块的正则表达式模式
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    /**
     * 解析单文件代码的流式输出结果
     *
     * @param streamResponse 流式响应内容
     * @return 解析后的单文件代码结果
     */
    public static SingleHtmlFileCodeResult parseSingleFileStream(String streamResponse) {
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

    /**
     * 解析多文件代码的流式输出结果
     *
     * @param streamResponse 流式响应内容
     * @return 解析后的多文件代码结果
     */
    public static MultiHtmlFileCodeResult parseMultiFileStream(String streamResponse) {
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

    /**
     * 从流式响应中提取JSON内容
     *
     * @param streamResponse 流式响应内容
     * @return 提取的JSON内容
     */
    private static String extractJsonContent(String streamResponse) {
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
    private static String ensureHtmlFileReferences(String htmlCode) {
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
            newHead.append("<head>\n");
            newHead.append("    <meta charset=\"UTF-8\">\n");
            newHead.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            newHead.append("    <title>生成页面</title>\n");
            
            // 添加CSS引用（如果不存在）
            if (!hasCssLink) {
                newHead.append("    <link rel=\"stylesheet\" href=\"style.css\">\n");
            }
            
            newHead.append(headContent);
            newHead.append("</head>");
            
            // 替换head部分
            htmlBuilder.append(htmlCode.substring(0, headStart));
            htmlBuilder.append(newHead.toString());
            htmlBuilder.append(htmlCode.substring(headEnd + 7));
            
            // 在body结束前添加JS引用（如果不存在）
            if (!hasJsScript) {
                int bodyEnd = htmlBuilder.indexOf("</body>");
                if (bodyEnd != -1) {
                    htmlBuilder.insert(bodyEnd, "\n    <script src=\"script.js\"></script>\n");
                } else {
                    // 如果没有body标签，在末尾添加
                    htmlBuilder.append("\n<script src=\"script.js\"></script>\n");
                }
            }
            
            return htmlBuilder.toString();
        } else {
            // 如果没有head标签，创建完整的HTML结构
            return createDefaultHtmlWithReferences();
        }
    }

    /**
     * 创建默认的单文件代码结果
     *
     * @return 默认的单文件代码结果
     */
    private static SingleHtmlFileCodeResult createDefaultSingleFileResult() {
        SingleHtmlFileCodeResult result = new SingleHtmlFileCodeResult();
        result.setHtmlCode("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>默认页面</title>\n</head>\n<body>\n    <h1>代码生成失败</h1>\n    <p>请检查AI返回结果格式是否正确。</p>\n</body>\n</html>");
        return result;
    }

    /**
     * 创建默认的多文件代码结果
     *
     * @return 默认的多文件代码结果
     */
    private static MultiHtmlFileCodeResult createDefaultMultiFileResult() {
        MultiHtmlFileCodeResult result = new MultiHtmlFileCodeResult();
        result.setHtmlCode(createDefaultHtmlWithReferences());
        result.setCssCode("/* 默认样式 */\nbody { margin: 0; padding: 20px; font-family: Arial, sans-serif; }\nh1 { color: #333; }");
        result.setJsCode("// 默认JavaScript代码\nconsole.log('页面加载完成');\ndocument.addEventListener('DOMContentLoaded', function() {\n    console.log('DOM已加载');\n});");
        result.setDescription("默认生成的代码");
        return result;
    }

    /**
     * 创建包含正确文件引用的默认HTML
     *
     * @return 默认HTML代码
     */
    private static String createDefaultHtmlWithReferences() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>生成页面</title>\n" +
                "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>默认页面</h1>\n" +
                "    <p>这是默认生成的页面。</p>\n" +
                "    <script src=\"script.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }
}