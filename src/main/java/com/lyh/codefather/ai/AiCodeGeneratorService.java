package com.lyh.codefather.ai;

import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

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

    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/code-generator-singleFilePrompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/code-generator-multiFilePrompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);


    @SystemMessage(fromResource = "prompt/code-generator-vue-project-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@UserMessage String userMessage);
}
