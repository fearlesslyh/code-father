package com.lyh.codefather.ai.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * HTML单文件代码保存器
 * 继承模板抽象类，实现单文件代码的保存逻辑
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
public class SingleFileCodeSaver extends CodeFileSaverTemplate<SingleHtmlFileCodeResult> {

    @Override
    protected boolean validateData(SingleHtmlFileCodeResult data) {
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
    protected void saveFiles(SingleHtmlFileCodeResult data, String dirPath) {
        // 保存HTML文件
        String htmlFilePath = dirPath + File.separator + "index.html";
        FileUtil.writeString(data.getHtmlCode(), htmlFilePath, StandardCharsets.UTF_8);
        log.info("HTML文件保存成功: {}", htmlFilePath);
    }

    @Override
    public String getSupportedType() {
        return "html";
    }
}