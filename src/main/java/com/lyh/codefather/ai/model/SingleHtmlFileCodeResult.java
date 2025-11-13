package com.lyh.codefather.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成单文件代码结果")
@Data
public class SingleHtmlFileCodeResult {

    @Description("html代码")
    private String htmlCode;

    @Description("css代码")
    private String description;
}
