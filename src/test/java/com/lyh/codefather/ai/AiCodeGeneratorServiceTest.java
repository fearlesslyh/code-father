package com.lyh.codefather.ai;

import com.lyh.codefather.ai.core.AiCodeGeneratorFacade;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/13 17:05
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateSingleFile() {
        File file = aiCodeGeneratorFacade.generateAndSave("做一个任务记录网站，代码全部放在一个html文件里，样式要酷炫牛逼", CodeGenTypeEnum.HTML);
        assertNotNull(file);
    }

    @Test
    void generateMultipleFiles() {
        File file = aiCodeGeneratorFacade.generateAndSave("做一个任务记录网站，样式要酷炫", CodeGenTypeEnum.MULTI_FILE);
        assertNotNull(file);
    }
}