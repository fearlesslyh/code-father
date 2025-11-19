package com.lyh.codefather.ai.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 多文件代码保存器
 * 继承模板抽象类，实现多文件代码的保存逻辑
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
public class MultiFileCodeSaver extends CodeFileSaverTemplate<MultiHtmlFileCodeResult> {

    @Override
    protected boolean validateData(MultiHtmlFileCodeResult data) {
        if (data == null) {
            log.warn("数据为空");
            return false;
        }
        
        if (StrUtil.isBlank(data.getHtmlCode())) {
            log.warn("HTML代码为空");
            return false;
        }
        
        return true;
    }

    @Override
    protected void saveFiles(MultiHtmlFileCodeResult data, String dirPath) {
        // 保存HTML文件
        String htmlFilePath = dirPath + File.separator + "index.html";
        FileUtil.writeString(data.getHtmlCode(), htmlFilePath, StandardCharsets.UTF_8);
        log.info("HTML文件保存成功: {}", htmlFilePath);
        
        // 保存CSS文件
        if (StrUtil.isNotBlank(data.getCssCode())) {
            String cssFilePath = dirPath + File.separator + "style.css";
            FileUtil.writeString(data.getCssCode(), cssFilePath, StandardCharsets.UTF_8);
            log.info("CSS文件保存成功: {}", cssFilePath);
        }
        
        // 保存JS文件
        if (StrUtil.isNotBlank(data.getJsCode())) {
            String jsFilePath = dirPath + File.separator + "script.js";
            FileUtil.writeString(data.getJsCode(), jsFilePath, StandardCharsets.UTF_8);
            log.info("JS文件保存成功: {}", jsFilePath);
        }
    }

    @Override
    public String getSupportedType() {
        return "multi_file";
    }
}