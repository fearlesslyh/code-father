package com.lyh.codefather.ai.model;

import jdk.jfr.Description;
import lombok.Data;

@Description("生成多文件代码结果")
@Data
public class MultiHtmlFileCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("css代码")
    private String cssCode;

    @Description("js代码")
    private String jsCode;

    @Description("代码的描述")
    private String description;
}
