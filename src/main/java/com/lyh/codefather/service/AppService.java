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
     * 更新应用（用户）
     *
     * @param appUpdateRequest 应用更新请求
     * @param userId           操作用户ID
     * @return 是否更新成功
     */
    boolean updateApp(AppUpdateRequest appUpdateRequest, long userId);

    /**
     * 更新应用（管理员）
     *
     * @param appUpdateRequest 应用更新请求
     * @return 是否更新成功
     */
    boolean updateAppByAdmin(AppUpdateRequest appUpdateRequest);

    /**
     * 删除应用（用户）
     *
     * @param appId  应用ID
     * @param userId 操作用户ID
     * @return 是否删除成功
     */
    boolean deleteApp(long appId, long userId);

    /**
     * 删除应用（管理员）
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteAppByAdmin(long appId);

    /**
     * 根据ID获取应用视图对象（用户）
     *
     * @param appId  应用ID
     * @param userId 操作用户ID
     * @return 应用视图对象
     */
    AppVO getAppVOById(long appId, long userId);

    /**
     * 根据ID获取应用视图对象（管理员）
     *
     * @param appId 应用ID
     * @return 应用视图对象
     */
    AppVO getAppVOByIdByAdmin(long appId);

    /**
     * 分页查询应用列表（用户）
     *
     * @param appQueryRequest 查询请求
     * @param userId          操作用户ID
     * @return 分页结果
     */
    Page<AppVO> listAppVOByPage(AppQueryRequest appQueryRequest, long userId);

    /**
     * 分页查询精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    Page<AppVO> listFeaturedAppVOByPage(AppQueryRequest appQueryRequest);

    /**
     * 分页查询应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    Page<AppVO> listAppVOByPageByAdmin(AppQueryRequest appQueryRequest);

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