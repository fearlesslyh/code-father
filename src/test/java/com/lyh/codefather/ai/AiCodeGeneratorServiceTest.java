package com.lyh.codefather.ai;

import com.lyh.codefather.ai.core.AiCodeGeneratorFacade;
import com.lyh.codefather.ai.core.CodeParser;
import com.lyh.codefather.ai.core.StreamCodeProcessor;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 2.0
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

    // ========== 流式输出解析功能测试 ==========

    @Test
    void testParseSingleFileStream_ValidJson() {
        // 模拟AI返回的JSON格式流式输出
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>测试页面</title></head><body><h1>Hello World</h1></body></html>",
                "cssCode": "body { margin: 0; padding: 20px; }"
            }
            """;

        SingleHtmlFileCodeResult result = CodeParser.parseSingleFileStream(streamResponse);
        
        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
        assertTrue(result.getCssCode().contains("body { margin: 0;"));
    }

    @Test
    void testParseSingleFileStream_WithCodeBlock() {
        // 模拟包含代码块的流式输出
        String streamResponse = """
            这是AI返回的内容：
            ```json
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>代码块测试</title></head><body><p>测试内容</p></body></html>",
                "cssCode": "p { color: blue; }"
            }
            ```
            以上是生成的代码
            """;

        SingleHtmlFileCodeResult result = CodeParser.parseSingleFileStream(streamResponse);
        
        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
        assertTrue(result.getCssCode().contains("color: blue"));
    }

    @Test
    void testParseMultiFileStream_ValidJson() {
        // 模拟多文件代码的JSON格式流式输出
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>多文件测试</title></head><body><div id='app'></div></body></html>",
                "cssCode": "#app { width: 100%; height: 100vh; }",
                "jsCode": "document.getElementById('app').innerHTML = 'Hello World';",
                "description": "这是一个多文件代码测试"
            }
            """;

        MultiHtmlFileCodeResult result = CodeParser.parseMultiFileStream(streamResponse);
        
        assertNotNull(result);
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
        assertTrue(result.getCssCode().contains("#app"));
        assertTrue(result.getJsCode().contains("getElementById"));
        assertEquals("这是一个多文件代码测试", result.getDescription());
    }

    @Test
    void testParseMultiFileStream_EnsureFileReferences() {
        // 测试HTML文件引用自动修正功能
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>引用测试</title></head><body><h1>测试</h1></body></html>",
                "cssCode": "h1 { color: red; }",
                "jsCode": "console.log('test');"
            }
            """;

        MultiHtmlFileCodeResult result = CodeParser.parseMultiFileStream(streamResponse);
        
        assertNotNull(result);
        // 确保HTML中自动添加了CSS和JS引用
        assertTrue(result.getHtmlCode().contains("style.css"));
        assertTrue(result.getHtmlCode().contains("script.js"));
    }

    @Test
    void testParseSingleFileStream_InvalidJson() {
        // 测试无效JSON的处理
        String streamResponse = "这不是一个有效的JSON格式";
        
        SingleHtmlFileCodeResult result = CodeParser.parseSingleFileStream(streamResponse);
        
        assertNotNull(result);
        // 应该返回默认的HTML代码
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
    }

    @Test
    void testProcessSingleFileStream() {
        // 测试完整的单文件流式处理流程
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>流程测试</title></head><body><h1>流程测试</h1></body></html>",
                "cssCode": "h1 { font-size: 24px; }"
            }
            """;

        File savedDir = StreamCodeProcessor.processSingleFileStream(streamResponse);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
        assertTrue(savedDir.isDirectory());
    }

    @Test
    void testProcessMultiFileStream() {
        // 测试完整的多文件流式处理流程
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>多文件流程测试</title></head><body><div class='container'></div></body></html>",
                "cssCode": ".container { max-width: 1200px; margin: 0 auto; }",
                "jsCode": "document.querySelector('.container').innerHTML = '多文件测试';"
            }
            """;

        File savedDir = StreamCodeProcessor.processMultiFileStream(streamResponse);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
        assertTrue(savedDir.isDirectory());
    }

    @Test
    void testProcessStreamByType_SingleFile() {
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>类型测试</title></head><body><p>单文件类型测试</p></body></html>",
                "cssCode": "p { margin: 10px; }"
            }
            """;

        File savedDir = StreamCodeProcessor.processStreamByType(streamResponse, CodeGenTypeEnum.HTML);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
    }

    @Test
    void testProcessStreamByType_MultiFile() {
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>类型测试</title></head><body><span>多文件类型测试</span></body></html>",
                "cssCode": "span { display: inline-block; }",
                "jsCode": "console.log('multi file test');"
            }
            """;

        File savedDir = StreamCodeProcessor.processStreamByType(streamResponse, CodeGenTypeEnum.MULTI_FILE);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
    }

    @Test
    void testIsValidStreamResponse() {
        // 测试有效响应
        String validResponse = "{\"htmlCode\": \"test\"}";
        assertTrue(StreamCodeProcessor.isValidStreamResponse(validResponse));
        
        // 测试无效响应（空内容）
        String emptyResponse = "";
        assertFalse(StreamCodeProcessor.isValidStreamResponse(emptyResponse));
        
        // 测试无效响应（不含JSON）
        String noJsonResponse = "这不是JSON格式";
        assertFalse(StreamCodeProcessor.isValidStreamResponse(noJsonResponse));
    }

    @Test
    void testGetPreviewInfo() {
        String streamResponse = """
            这是AI返回的流式输出：
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>预览测试</title></head><body><h1>预览内容</h1></body></html>",
                "cssCode": "h1 { color: green; }"
            }
            """;

        String preview = StreamCodeProcessor.getPreviewInfo(streamResponse);
        
        assertNotNull(preview);
        assertTrue(preview.contains("htmlCode"));
        assertTrue(preview.contains("cssCode"));
    }

    @Test
    void testFacadeProcessStreamAndSave_SingleFile() {
        // 测试门面类的流式处理方法
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>门面测试</title></head><body><h2>门面单文件测试</h2></body></html>",
                "cssCode": "h2 { font-weight: bold; }"
            }
            """;

        File savedDir = aiCodeGeneratorFacade.processStreamAndSave(streamResponse, CodeGenTypeEnum.HTML);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
    }

    @Test
    void testFacadeProcessStreamAndSave_MultiFile() {
        // 测试门面类的多文件流式处理方法
        String streamResponse = """
            {
                "htmlCode": "<!DOCTYPE html><html><head><title>门面多文件测试</title></head><body><section></section></body></html>",
                "cssCode": "section { padding: 20px; }",
                "jsCode": "document.querySelector('section').textContent = '门面多文件测试';"
            }
            """;

        File savedDir = aiCodeGeneratorFacade.processStreamAndSave(streamResponse, CodeGenTypeEnum.MULTI_FILE);
        
        assertNotNull(savedDir);
        assertTrue(savedDir.exists());
    }
}