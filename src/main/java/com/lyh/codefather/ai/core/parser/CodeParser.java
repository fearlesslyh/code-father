package com.lyh.codefather.ai.core.parser;

/**
 * 代码解析器接口
 * 通过泛型统一方法的返回值
 *
 * @param <T> 解析结果类型
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
public interface CodeParser<T> {

    /**
     * 解析流式输出结果
     *
     * @param streamResponse 流式响应内容
     * @return 解析后的结果
     */
    T parseStream(String streamResponse);

    /**
     * 获取解析器支持的类型
     *
     * @return 支持的类型
     */
    String getSupportedType();
}