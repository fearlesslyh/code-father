package com.lyh.codefather.constant;

/**
 * 应用常量类
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
public interface AppConstant {

    /**
     * 精选应用优先级
     */
    int FEATURED_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    int DEFAULT_PRIORITY = 0;

    /**
     * 应用名称最大长度
     */
    int MAX_APP_NAME_LENGTH = 50;

    /**
     * 初始化提示词最大长度
     */
    int MAX_INIT_PROMPT_LENGTH = 1000;

    /**
     * 应用封面URL最大长度
     */
    int MAX_COVER_URL_LENGTH = 500;

    /**
     * 默认代码生成类型
     */
    String DEFAULT_CODE_GEN_TYPE = "html";
}