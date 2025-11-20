package com.lyh.codefather.ai.core.saver;

import com.lyh.codefather.ai.model.MultiHtmlFileCodeResult;
import com.lyh.codefather.ai.model.SingleHtmlFileCodeResult;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import com.lyh.codefather.exception.BusinessException;
import com.lyh.codefather.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码文件保存执行器
 * 实现执行器模式，根据代码生成类型执行相应的保存逻辑
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/19
 */
@Slf4j
@Component
public class CodeFileSaverExecutor {

    private final Map<String, CodeFileSaverTemplate<?>> saverMap = new HashMap<>();

    public CodeFileSaverExecutor() {
        // 初始化所有保存器
        registerSaver(new SingleFileCodeSaver());
        registerSaver(new MultiFileCodeSaver());
    }

    /**
     * 注册保存器
     *
     * @param saver 保存器实例
     */
    private void registerSaver(CodeFileSaverTemplate<?> saver) {
        saverMap.put(saver.getSupportedType(), saver);
        log.info("注册保存器: {}", saver.getSupportedType());
    }

    /**
     * 根据类型保存代码
     *
     * @param data 要保存的数据
     * @param type 生成类型
     * @param appId 应用ID
     * @return 保存的目录
     */
    @SuppressWarnings("unchecked")
    public <T> File saveByType(T data, String type, Long appId) {
        // 参数校验
        if (appId == null || appId <= 0) {
            log.error("应用ID无效: {}", appId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        log.info("使用保存器处理类型: {}, 应用ID: {}", type, appId);
        
        CodeFileSaverTemplate<T> saver = (CodeFileSaverTemplate<T>) saverMap.get(type);
        if (saver == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的保存类型: " + type);
        }
        
        return saver.saveCode(data, appId);
    }

    /**
     * 保存单文件代码
     *
     * @param data 单文件代码结果
     * @param appId 应用ID
     * @return 保存的目录
     */
    public File saveSingleFile(SingleHtmlFileCodeResult data, Long appId) {
        return saveByType(data, CodeGenTypeEnum.HTML.getValue(), appId);
    }

    /**
     * 保存多文件代码
     *
     * @param data 多文件代码结果
     * @param appId 应用ID
     * @return 保存的目录
     */
    public File saveMultiFile(MultiHtmlFileCodeResult data, Long appId) {
        return saveByType(data, CodeGenTypeEnum.MULTI_FILE.getValue(), appId);
    }
}