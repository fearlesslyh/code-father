package com.lyh.codefather.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.codefather.ai.AiCodeGeneratorService;
import com.lyh.codefather.ai.core.AiCodeGeneratorFacade;
import com.lyh.codefather.ai.model.enums.CodeGenTypeEnum;
import com.lyh.codefather.constant.AppConstant;
import com.lyh.codefather.exception.BusinessException;
import com.lyh.codefather.exception.ErrorCode;
import com.lyh.codefather.exception.ThrowUtils;
import com.lyh.codefather.mapper.AppMapper;
import com.lyh.codefather.model.dto.app.AppAddRequest;
import com.lyh.codefather.model.dto.app.AppQueryRequest;
import com.lyh.codefather.model.dto.app.AppUpdateRequest;
import com.lyh.codefather.model.entity.App;
import com.lyh.codefather.model.entity.User;
import com.lyh.codefather.model.vo.AppVO;
import com.lyh.codefather.model.vo.UserVO;
import com.lyh.codefather.service.AppService;
import com.lyh.codefather.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mybatisflex.core.paginate.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Override
    public long createApp(AppAddRequest appAddRequest, long userId) {
        // 参数校验
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
        ThrowUtils.throwIf(initPrompt.length() > AppConstant.MAX_INIT_PROMPT_LENGTH, 
                ErrorCode.PARAMS_ERROR, "初始化提示词过长");

        // 检查用户是否存在
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 创建应用
        App app = new App();
        // 自动生成应用名称（取提示词前12位）
        String appName = initPrompt.length() > 12 ? initPrompt.substring(0, 12) : initPrompt;
        app.setAppName(appName);
        app.setInitPrompt(initPrompt);
        // 设置默认的代码生成类型
        app.setCodeGenType(AppConstant.DEFAULT_CODE_GEN_TYPE);
        app.setPriority(AppConstant.DEFAULT_PRIORITY);
        app.setUserId(userId);
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        app.setEditTime(LocalDateTime.now());

        // 保存应用
        boolean saveResult = this.save(app);
        ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "创建应用失败");

        return app.getId();
    }

    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest, long userId) {
        // 参数校验
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        // 查询应用
        App oldApp = this.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限校验：只能修改自己的应用
        ThrowUtils.throwIf(!Objects.equals(oldApp.getUserId(), userId), ErrorCode.NO_AUTH_ERROR, "无权限修改此应用");
        
        // 更新应用
        App updateApp = new App();
        updateApp.setId(appUpdateRequest.getId());
        
        // 只允许修改应用名称
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > AppConstant.MAX_APP_NAME_LENGTH, 
                    ErrorCode.PARAMS_ERROR, "应用名称过长");
            updateApp.setAppName(appName);
        }
        
        // 更新编辑时间和系统更新时间
        updateApp.setEditTime(LocalDateTime.now());
        updateApp.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(updateApp);
    }

    @Override
    public boolean updateAppByAdmin(AppUpdateRequest appUpdateRequest) {
        // 参数校验
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        // 查询应用
        App oldApp = this.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 更新应用
        App updateApp = new App();
        updateApp.setId(appUpdateRequest.getId());
        
        // 管理员可以更新应用名称、应用封面和优先级
        String appName = appUpdateRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > AppConstant.MAX_APP_NAME_LENGTH, 
                    ErrorCode.PARAMS_ERROR, "应用名称过长");
            updateApp.setAppName(appName);
        }
        
        String cover = appUpdateRequest.getCover();
        if (StrUtil.isNotBlank(cover)) {
            ThrowUtils.throwIf(cover.length() > AppConstant.MAX_COVER_URL_LENGTH, 
                    ErrorCode.PARAMS_ERROR, "应用封面URL过长");
            updateApp.setCover(cover);
        }
        
        Integer priority = appUpdateRequest.getPriority();
        if (priority != null) {
            updateApp.setPriority(priority);
        }
        
        // 更新系统更新时间
        updateApp.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(updateApp);
    }

    @Override
    public boolean deleteApp(long appId, long userId) {
        // 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限校验：只能删除自己的应用
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), userId), ErrorCode.NO_AUTH_ERROR, "无权限删除此应用");
        
        return this.removeById(appId);
    }

    @Override
    public boolean deleteAppByAdmin(long appId) {
        // 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        return this.removeById(appId);
    }

    @Override
    public AppVO getAppVOById(long appId, long userId) {
        // 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限校验：只能查看自己的应用
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), userId), ErrorCode.NO_AUTH_ERROR, "无权限查看此应用");
        
        return getAppVO(app);
    }

    @Override
    public AppVO getAppVOByIdByAdmin(long appId) {
        // 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        return getAppVO(app);
    }

    @Override
    public Page<AppVO> listAppVOByPage(AppQueryRequest appQueryRequest, long userId) {
        // 设置查询条件：只能查询自己的应用
        AppQueryRequest queryRequest = new AppQueryRequest();
        BeanUtil.copyProperties(appQueryRequest, queryRequest);
        queryRequest.setUserId(userId);
        
        // 限制每页最多20个
        if (queryRequest.getPageSize() == 0 || queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        
        // 构建查询条件
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        
        // 分页查询
        Page<App> appPage = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), queryWrapper);
        
        // 转换为VO
        return convertToVOPage(appPage);
    }

    @Override
    public Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest) {
        // 设置查询条件：只查询精选应用
        AppQueryRequest queryRequest = new AppQueryRequest();
        BeanUtil.copyProperties(appQueryRequest, queryRequest);
        queryRequest.setPriority(AppConstant.FEATURED_PRIORITY);
        
        // 限制每页最多20个
        if (queryRequest.getPageSize() == 0 || queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        
        // 构建查询条件
        QueryWrapper queryWrapper = getQueryWrapper(queryRequest);
        
        // 分页查询
        Page<App> appPage = this.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), queryWrapper);
        
        // 转换为VO
        return convertToVOPage(appPage);
    }

    @Override
    public Page<AppVO> listAppVOByPageByAdmin(AppQueryRequest appQueryRequest) {
        // 构建查询条件
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        
        // 分页查询（管理员每页数量不限）
        Page<App> appPage = this.page(Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()), queryWrapper);
        
        // 转换为VO
        return convertToVOPage(appPage);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        
        // 获取用户信息
        User user = userService.getById(app.getUserId());
        if (user != null) {
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        
        // 1. 收集所有userId到集合中
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        
        // 2. 根据userId集合批量查询所有用户信息
        List<User> userList = userService.listByIds(userIdSet);
        
        // 3. 构建Map映射关系userId -> UserVO
        Map<Long, UserVO> userVOMap;
        if (!userList.isEmpty()) {
            List<UserVO> userVOList = userService.getUserVOList(userList);
            userVOMap = userVOList.stream()
                    .collect(Collectors.toMap(UserVO::getId, userVO -> userVO));
        } else {
            userVOMap = new HashMap<>();
        }

        // 4. 一次性封装所有AppVO，根据userId从Map中获取需要的用户信息
        return appList.stream()
                .map(app -> {
                    AppVO appVO = new AppVO();
                    BeanUtil.copyProperties(app, appVO);
                    appVO.setUser(userVOMap.get(app.getUserId()));
                    return appVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        
        // 构建查询条件
        return QueryWrapper.create()
                .eq("id", id, id != null)
                .like("appName", appName)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 将App分页对象转换为AppVO分页对象
     *
     * @param appPage App分页对象
     * @return AppVO分页对象
     */
    private Page<AppVO> convertToVOPage(Page<App> appPage) {
        // 转换为VO列表
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());

        // 创建新的分页对象
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        appVOPage.setRecords(appVOList);
        
        return appVOPage;
    }

    @Override
    public Flux<String> chatToGenCodeStream(Long appId, String userMessage, String codeGenType, Long userId) {
        // 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        if (StrUtil.isBlank(userMessage)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户输入消息不能为空");
        }
        
        if (StrUtil.isBlank(codeGenType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        
        // 验证应用权限
        validateAppPermission(appId, userId);
        
        // 转换代码生成类型
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            log.error("不支持的代码生成类型: {}", codeGenType);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型: " + codeGenType);
        }
        
        // 根据类型选择流式生成方法
        if (CodeGenTypeEnum.HTML.equals(codeGenTypeEnum)) {
            return aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        } else {
            return aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        }
    }
    
    @Override
    public SseEmitter createCodeGenEmitter(Long appId, String userMessage, String codeGenType, Long userId) {
        // 创建SSE发射器，设置超时时间为30分钟
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(30 * 60 * 1000L);
        
        // 设置完成、超时和错误时的回调
        emitter.onCompletion(() -> log.info("SSE连接完成，应用ID: {}, 用户ID: {}", appId, userId));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，应用ID: {}, 用户ID: {}", appId, userId);
            emitter.complete();
        });
        emitter.onError((ex) -> log.error("SSE连接出错，应用ID: {}, 用户ID: {}", appId, userId, ex));
        
        // 在新线程中执行代码生成和流式返回
        new Thread(() -> {
            try {
                // 发送开始事件
                sendEvent(emitter, "start", "{\"status\":\"started\",\"message\":\"代码生成开始\"}");
                
                // 获取流式代码生成结果
                reactor.core.publisher.Flux<String> flux = chatToGenCodeStream(appId, userMessage, codeGenType, userId);
                
                // 订阅流并逐块发送数据
                flux.subscribe(
                    chunk -> {
                        try {
                            // 使用Base64编码确保空格和特殊字符不会丢失
                            String encodedChunk = java.util.Base64.getEncoder().encodeToString(chunk.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            sendEvent(emitter, "message", encodedChunk);
                        } catch (Exception e) {
                            log.error("发送SSE消息失败", e);
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        log.error("代码生成流式处理出错", error);
                        sendErrorEvent(emitter, error.getMessage());
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            // 流式处理完成，保存代码并返回结果
                            CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
                            File savedDir = aiCodeGeneratorFacade.generateAndSave(userMessage, codeGenTypeEnum, appId);
                            
                            String completeMessage = String.format(
                                "{\"status\":\"completed\",\"message\":\"代码生成完成\",\"directory\":\"%s\"}", 
                                savedDir.getAbsolutePath()
                            );
                            sendEvent(emitter, "complete", completeMessage);
                            
                            // 发送明确的流结束通知
                            sendEvent(emitter, "done", "{\"status\":\"done\",\"message\":\"流式传输已完成\"}");
                            
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("保存代码失败", e);
                            sendErrorEvent(emitter, "保存代码失败: " + e.getMessage());
                            emitter.completeWithError(e);
                        }
                    }
                );
                
            } catch (Exception e) {
                log.error("SSE流处理异常", e);
                sendErrorEvent(emitter, e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();
        
        return emitter;
    }
    
    /**
     * 验证用户是否有权限使用应用生成代码
     *
     * @param appId 应用ID
     * @param userId 用户ID
     * @return 应用实体
     */
    private App validateAppPermission(Long appId, Long userId) {
        // 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限校验：只能使用自己的应用生成代码
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), userId), ErrorCode.NO_AUTH_ERROR, "无权限使用此应用生成代码");
        
        return app;
    }
    
    /**
     * 发送SSE事件
     *
     * @param emitter SSE发射器
     * @param eventName 事件名称
     * @param data 事件数据
     */
    private void sendEvent(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name(eventName)
                    .data(data)
                    .id(String.valueOf(System.currentTimeMillis())));
        } catch (Exception e) {
            log.error("发送SSE消息失败，事件名称: {}", eventName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送SSE消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送错误事件
     *
     * @param emitter SSE发射器
     * @param errorMessage 错误消息
     */
    private void sendErrorEvent(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, String errorMessage) {
        try {
            String errorData = String.format("{\"status\":\"error\",\"message\":\"%s\"}", errorMessage);
            sendEvent(emitter, "error", errorData);
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }
}