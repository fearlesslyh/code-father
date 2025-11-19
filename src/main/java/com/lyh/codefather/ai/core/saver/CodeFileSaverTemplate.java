package com.lyh.codefather.ai.core.saver;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 代码文件保存模板抽象类
 * 实现模板方法模式，定义通用的文件保存流程
 *
 * @param <T> 保存的数据类型
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String SAVE_ROOT_PATH = System.getProperty("user.dir") + "/src/main/resources/tmp/code_generate";

    /**
     * 保存代码的模板方法
     * 定义保存代码的通用流程，子类可以有自己的实现
     *
     * @param data 要保存的数据
     * @return 保存的目录
     */
    public final File saveCode(T data) {
        log.info("开始保存代码，类型: {}", getSupportedType());
        
        // 1. 验证数据
        if (!validateData(data)) {
            log.error("数据验证失败");
            throw new IllegalArgumentException("数据验证失败");
        }
        
        // 2. 创建唯一目录
        String dirPath = buildUniqueDir();
        
        // 3. 保存文件（由子类实现）
        saveFiles(data, dirPath);
        
        // 4. 返回保存的目录
        File savedDir = new File(dirPath);
        log.info("代码保存完成，目录: {}", dirPath);
        return savedDir;
    }

    /**
     * 验证数据有效性
     *
     * @param data 要验证的数据
     * @return 验证结果
     */
    protected abstract boolean validateData(T data);

    /**
     * 保存文件
     *
     * @param data    要保存的数据
     * @param dirPath 保存目录路径
     */
    protected abstract void saveFiles(T data, String dirPath);

    /**
     * 获取支持的类型
     *
     * @return 支持的类型
     */
    public abstract String getSupportedType();

    /**
     * 构建唯一目录的路径
     *
     * @return 目录路径
     */
    private String buildUniqueDir() {
        String dirName = StrUtil.format("{}_{}", getSupportedType(), IdUtil.getSnowflakeNextIdStr());
        String dirPath = SAVE_ROOT_PATH + File.separator + dirName;
        
        // 创建目录
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                log.error("创建目录失败: {}", dirPath);
                throw new RuntimeException("创建目录失败: " + dirPath);
            }
        }
        
        return dirPath;
    }
}