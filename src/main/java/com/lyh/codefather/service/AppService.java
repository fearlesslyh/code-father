package com.lyh.codefather.service;

import com.lyh.codefather.model.dto.app.AppAddRequest;
import com.lyh.codefather.model.dto.app.AppQueryRequest;
import com.lyh.codefather.model.dto.app.AppUpdateRequest;
import com.lyh.codefather.model.entity.App;
import com.lyh.codefather.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest 应用创建请求
     * @param userId        创建用户ID
     * @return 应用ID
     */
    long createApp(AppAddRequest appAddRequest, long userId);


    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 通过对话生成代码（流式处理）
     *
     * @param appId       应用ID
     * @param userMessage 用户输入消息
     * @param codeGenType 代码生成类型
     * @param userId      操作用户ID
     * @return 流式代码生成结果
     */
    Flux<String> chatToGenCodeStream(Long appId, String userMessage, String codeGenType, Long userId);

    /**
     * 创建SSE发射器并处理流式代码生成
     *
     * @param appId       应用ID
     * @param userMessage 用户输入消息
     * @param codeGenType 代码生成类型
     * @param userId      用户ID
     * @return SSE发射器
     */
    SseEmitter createCodeGenEmitter(Long appId, String userMessage, String codeGenType, Long userId);
}