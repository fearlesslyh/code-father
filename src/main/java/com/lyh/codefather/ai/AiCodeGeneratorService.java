package com.lyh.codefather.ai;

import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {

    String generateCode(String userMessage);

    /**
     * 生成单HTML文件代码
     *
     * @param userMessage 用户输入
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/code-generator-singleFilePrompt.txt")
    SingleHtmlFileCodeResult generateSingleFile(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户输入
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/code-generator-multiFilePrompt.txt")
    MultiHtmlFileCodeResult generateMultipleFiles(String userMessage);
}
