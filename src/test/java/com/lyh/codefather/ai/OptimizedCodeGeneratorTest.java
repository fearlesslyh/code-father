package com.lyh.codefather.ai;

import com.lyh.codefather.ai.core.AiCodeGeneratorFacade;
import com.lyh.codefather.ai.core.parser.CodeParserExecutor;
import com.lyh.codefather.ai.core.saver.CodeFileSaverExecutor;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * 优化后的代码生成器测试类
 * 测试执行器模式、策略模式和模板方法模式的实现
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
@SpringBootTest
public class OptimizedCodeGeneratorTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    
    @Resource
    private CodeParserExecutor codeParserExecutor;
    
    @Resource
    private CodeFileSaverExecutor codeFileSaverExecutor;

    /**
     * 测试单文件代码生成和保存
     */
    @Test
    public void testSingleFileGenerationAndSave() {
        String userMessage = "做一个简单的任务记录网站，代码全部放在一个html文件里，样式要酷炫牛逼";
        
        try {
            File result = aiCodeGeneratorFacade.generateAndSave(userMessage, CodeGenTypeEnum.HTML);
            log.info("单文件代码生成成功，保存路径: {}", result.getAbsolutePath());
            
            // 验证文件是否存在
            File htmlFile = new File(result, "index.html");
            assert htmlFile.exists() : "HTML文件不存在";
            log.info("HTML文件验证成功");
        } catch (Exception e) {
            log.error("单文件代码生成失败", e);
            throw e;
        }
    }

    /**
     * 测试多文件代码生成和保存
     */
    @Test
    public void testMultiFileGenerationAndSave() {
        String userMessage = "做一个任务记录网站，样式要酷炫，使用HTML、CSS和JS分离的方式";
        
        try {
            File result = aiCodeGeneratorFacade.generateAndSave(userMessage, CodeGenTypeEnum.MULTI_FILE);
            log.info("多文件代码生成成功，保存路径: {}", result.getAbsolutePath());
            
            // 验证文件是否存在
            File htmlFile = new File(result, "index.html");
            File cssFile = new File(result, "style.css");
            File jsFile = new File(result, "script.js");
            
            assert htmlFile.exists() : "HTML文件不存在";
            assert cssFile.exists() : "CSS文件不存在";
            assert jsFile.exists() : "JS文件不存在";
            
            log.info("多文件验证成功");
        } catch (Exception e) {
            log.error("多文件代码生成失败", e);
            throw e;
        }
    }

    /**
     * 测试流式输出解析和保存
     */
    @Test
    public void testStreamProcessingAndSave() {
        // 模拟单文件流式输出
        String singleFileStreamResponse = "{\n" +
                "  \"htmlCode\": \"<!DOCTYPE html><html><head><title>任务记录</title></head><body><h1>任务列表</h1></body></html>\",\n" +
                "  \"cssCode\": \"body { font-family: Arial; }\"\n" +
                "}";
        
        try {
            File result = aiCodeGeneratorFacade.processStreamAndSave(singleFileStreamResponse, CodeGenTypeEnum.HTML);
            log.info("单文件流式处理成功，保存路径: {}", result.getAbsolutePath());
            
            // 验证文件是否存在
            File htmlFile = new File(result, "index.html");
            assert htmlFile.exists() : "HTML文件不存在";
            log.info("单文件流式处理验证成功");
        } catch (Exception e) {
            log.error("单文件流式处理失败", e);
            throw e;
        }
    }

    /**
     * 测试多文件流式输出解析和保存
     */
    @Test
    public void testMultiFileStreamProcessingAndSave() {
        // 模拟多文件流式输出
        String multiFileStreamResponse = "{\n" +
                "  \"htmlCode\": \"<!DOCTYPE html><html><head><title>任务记录</title><link rel=\\\"stylesheet\\\" href=\\\"style.css\\\"></head><body><h1>任务列表</h1><script src=\\\"script.js\\\"></script></body></html>\",\n" +
                "  \"cssCode\": \"body { font-family: Arial; margin: 0; padding: 20px; }\",\n" +
                "  \"jsCode\": \"console.log('页面加载完成');\",\n" +
                "  \"description\": \"一个简单的任务记录网站\"\n" +
                "}";
        
        try {
            File result = aiCodeGeneratorFacade.processStreamAndSave(multiFileStreamResponse, CodeGenTypeEnum.MULTI_FILE);
            log.info("多文件流式处理成功，保存路径: {}", result.getAbsolutePath());
            
            // 验证文件是否存在
            File htmlFile = new File(result, "index.html");
            File cssFile = new File(result, "style.css");
            File jsFile = new File(result, "script.js");
            
            assert htmlFile.exists() : "HTML文件不存在";
            assert cssFile.exists() : "CSS文件不存在";
            assert jsFile.exists() : "JS文件不存在";
            
            log.info("多文件流式处理验证成功");
        } catch (Exception e) {
            log.error("多文件流式处理失败", e);
            throw e;
        }
    }

    /**
     * 测试解析器执行器
     */
    @Test
    public void testParserExecutor() {
        // 测试单文件解析器
        String singleFileStreamResponse = "{\n" +
                "  \"htmlCode\": \"<!DOCTYPE html><html><head><title>测试</title></head><body><h1>测试页面</h1></body></html>\",\n" +
                "  \"cssCode\": \"body { font-family: Arial; }\"\n" +
                "}";
        
        try {
            SingleHtmlFileCodeResult singleResult = codeParserExecutor.parseSingleFile(singleFileStreamResponse);
            assert singleResult != null : "单文件解析结果为空";
            assert singleResult.getHtmlCode() != null : "HTML代码为空";
            log.info("单文件解析器测试成功");
        } catch (Exception e) {
            log.error("单文件解析器测试失败", e);
            throw e;
        }
        
        // 测试多文件解析器
        String multiFileStreamResponse = "{\n" +
                "  \"htmlCode\": \"<!DOCTYPE html><html><head><title>测试</title></head><body><h1>测试页面</h1></body></html>\",\n" +
                "  \"cssCode\": \"body { font-family: Arial; }\",\n" +
                "  \"jsCode\": \"console.log('测试');\",\n" +
                "  \"description\": \"测试页面\"\n" +
                "}";
        
        try {
            MultiHtmlFileCodeResult multiResult = codeParserExecutor.parseMultiFile(multiFileStreamResponse);
            assert multiResult != null : "多文件解析结果为空";
            assert multiResult.getHtmlCode() != null : "HTML代码为空";
            assert multiResult.getCssCode() != null : "CSS代码为空";
            assert multiResult.getJsCode() != null : "JS代码为空";
            log.info("多文件解析器测试成功");
        } catch (Exception e) {
            log.error("多文件解析器测试失败", e);
            throw e;
        }
    }

    /**
     * 测试文件保存器执行器
     */
    @Test
    public void testSaverExecutor() {
        // 测试单文件保存器
        SingleHtmlFileCodeResult singleResult = new SingleHtmlFileCodeResult();
        singleResult.setHtmlCode("<!DOCTYPE html><html><head><title>测试</title></head><body><h1>测试页面</h1></body></html>");
        singleResult.setCssCode("body { font-family: Arial; }");
        
        try {
            File singleDir = codeFileSaverExecutor.saveSingleFile(singleResult);
            assert singleDir.exists() : "单文件保存目录不存在";
            
            File htmlFile = new File(singleDir, "index.html");
            assert htmlFile.exists() : "HTML文件不存在";
            
            log.info("单文件保存器测试成功，保存路径: {}", singleDir.getAbsolutePath());
        } catch (Exception e) {
            log.error("单文件保存器测试失败", e);
            throw e;
        }
        
        // 测试多文件保存器
        MultiHtmlFileCodeResult multiResult = new MultiHtmlFileCodeResult();
        multiResult.setHtmlCode("<!DOCTYPE html><html><head><title>测试</title><link rel=\"stylesheet\" href=\"style.css\"></head><body><h1>测试页面</h1><script src=\"script.js\"></script></body></html>");
        multiResult.setCssCode("body { font-family: Arial; }");
        multiResult.setJsCode("console.log('测试');");
        multiResult.setDescription("测试页面");
        
        try {
            File multiDir = codeFileSaverExecutor.saveMultiFile(multiResult);
            assert multiDir.exists() : "多文件保存目录不存在";
            
            File htmlFile = new File(multiDir, "index.html");
            File cssFile = new File(multiDir, "style.css");
            File jsFile = new File(multiDir, "script.js");
            
            assert htmlFile.exists() : "HTML文件不存在";
            assert cssFile.exists() : "CSS文件不存在";
            assert jsFile.exists() : "JS文件不存在";
            
            log.info("多文件保存器测试成功，保存路径: {}", multiDir.getAbsolutePath());
        } catch (Exception e) {
            log.error("多文件保存器测试失败", e);
            throw e;
        }
    }
}