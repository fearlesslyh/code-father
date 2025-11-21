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
import java.io.IOException;
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
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
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
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
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
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 设置完成、超时和错误时的回调
        emitter.onCompletion(() -> log.info("SSE连接完成，应用ID: {}, 用户ID: {}", appId, userId));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时，应用ID: {}, 用户ID: {}", appId, userId);
            try {
                emitter.send(SseEmitter.event()
                        .name("timeout")
                        .data("{\"status\":\"timeout\",\"message\":\"连接超时\"}")
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException e) {
                log.error("发送超时消息失败", e);
            }
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("SSE连接出错，应用ID: {}, 用户ID: {}", appId, userId, ex);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"status\":\"error\",\"message\":\"" + ex.getMessage() + "\"}")
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException e) {
                log.error("发送错误消息失败", e);
            }
            emitter.completeWithError(ex);
        });

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
                                // 直接发送原始数据，不使用Base64编码
                                sendEvent(emitter, "message", chunk);
                            } catch (Exception e) {
                                log.error("发送SSE消息失败", e);
                                try {
                                    emitter.send(SseEmitter.event()
                                            .name("error")
                                            .data("{\"status\":\"error\",\"message\":\"发送消息失败\"}")
                                            .id(String.valueOf(System.currentTimeMillis())));
                                } catch (IOException ioException) {
                                    log.error("发送错误消息失败", ioException);
                                }
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
     * @param appId  应用ID
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
     * @param emitter   SSE发射器
     * @param eventName 事件名称
     * @param data      事件数据
     */
    private void sendEvent(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event()
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
     * @param emitter      SSE发射器
     * @param errorMessage 错误消息
     */
    private void sendErrorEvent(SseEmitter emitter, String errorMessage) {
        try {
            String errorData = String.format("{\"status\":\"error\",\"message\":\"%s\"}", errorMessage);
            sendEvent(emitter, "error", errorData);
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }
}