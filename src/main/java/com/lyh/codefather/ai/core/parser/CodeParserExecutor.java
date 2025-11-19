package com.lyh.codefather.ai.core.parser;

import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码解析执行器
 * 实现执行器模式，根据代码生成类型执行相应的解析逻辑
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
@Component
public class CodeParserExecutor {

    private final Map<String, CodeParser<?>> parserMap = new HashMap<>();

    public CodeParserExecutor() {
        // 初始化所有解析器
        registerParser(new SingleFileCodeParser());
        registerParser(new MultiFileCodeParser());
    }

    /**
     * 注册解析器
     *
     * @param parser 解析器实例
     */
    private void registerParser(CodeParser<?> parser) {
        parserMap.put(parser.getSupportedType(), parser);
        log.info("注册解析器: {}", parser.getSupportedType());
    }

    /**
     * 根据类型解析流式输出
     *
     * @param streamResponse 流式响应内容
     * @param type           生成类型
     * @return 解析结果
     */
    @SuppressWarnings("unchecked")
    public <T> T parseByType(String streamResponse, String type) {
        log.info("使用解析器处理类型: {}", type);
        
        CodeParser<T> parser = (CodeParser<T>) parserMap.get(type);
        if (parser == null) {
            throw new IllegalArgumentException("不支持的解析类型: " + type);
        }
        
        return parser.parseStream(streamResponse);
    }

    /**
     * 解析单文件代码
     *
     * @param streamResponse 流式响应内容
     * @return 单文件代码结果
     */
    public SingleHtmlFileCodeResult parseSingleFile(String streamResponse) {
        return parseByType(streamResponse, CodeGenTypeEnum.HTML.getValue());
    }

    /**
     * 解析多文件代码
     *
     * @param streamResponse 流式响应内容
     * @return 多文件代码结果
     */
    public MultiHtmlFileCodeResult parseMultiFile(String streamResponse) {
        return parseByType(streamResponse, CodeGenTypeEnum.MULTI_FILE.getValue());
    }
}