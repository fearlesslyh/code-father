package com.lyh.codefather.ai.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/13 20:53
 */
public class CodeFileSaver {
    // 文件保存根目录
    private static final String SAVE_ROOT_PATH = System.getProperty("user.dir") + "/src/main/resources/tmp/code_generate";

    /**
     * 保存单文件代码结果
     *
     * @param result 单文件代码结果
     * @return 保存的目录
     */
    public static File saveSingleHtmlCodeResult(SingleHtmlFileCodeResult result) {
        String dir = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        saveToFile(dir, "index.html", result.getHtmlCode());
        return new File(dir);
    }

    public static File saveMultiHtmlCodeResult(MultiHtmlFileCodeResult result) {
        String dir = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveToFile(dir, "index.html", result.getHtmlCode());
        saveToFile(dir, "style.css", result.getCssCode());
        saveToFile(dir, "script.js", result.getJsCode());
        return new File(dir);
    }

    /**
     * 构建唯一目录的路径，路径名，并创建该目录，返回该目录的路径。
     * 文件名规则：业务类型 + 雪花ID
     * 就像AI craft模式，先创建目录，再写入文件
     * @param bizType 业务类型
     * @return 目录路径
     */
    private static String buildUniqueDir(String bizType) {
        String dirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = SAVE_ROOT_PATH + File.separator + dirName;
        // 创建目录
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入文件
     *
     * @param dirPath  目录路径
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void saveToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        // 把内容写入文件，并放入目录中
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
