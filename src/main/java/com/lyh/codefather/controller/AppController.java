package com.lyh.codefather.controller;

import cn.hutool.core.bean.BeanUtil;
import com.lyh.codefather.annotation.AuthCheck;
import com.lyh.codefather.common.BaseResponse;
import com.lyh.codefather.common.DeleteRequest;
import com.lyh.codefather.common.ResultUtils;
import com.lyh.codefather.constant.UserConstant;
import com.lyh.codefather.exception.BusinessException;
import com.lyh.codefather.exception.ErrorCode;
import com.lyh.codefather.exception.ThrowUtils;
import com.lyh.codefather.model.dto.app.AppAddRequest;
import com.lyh.codefather.model.dto.app.AppQueryRequest;
import com.lyh.codefather.model.dto.app.AppUpdateRequest;
import com.lyh.codefather.model.entity.App;
import com.lyh.codefather.model.entity.User;
import com.lyh.codefather.model.vo.AppVO;
import com.lyh.codefather.service.AppService;
import com.lyh.codefather.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 应用 控制层。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建应用
     *
     * @param appAddRequest 应用创建请求
     * @param request       HTTP请求
     * @return 应用ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        long appId = appService.createApp(appAddRequest, loginUser.getId());
        return ResultUtils.success(appId);
    }

    /**
     * 更新应用（用户）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HTTP请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        boolean result = appService.updateApp(appUpdateRequest, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新应用（管理员）
     *
     * @param appUpdateRequest 应用更新请求
     * @return 是否更新成功
     */
    @PostMapping("/update/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppUpdateRequest appUpdateRequest) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        boolean result = appService.updateAppByAdmin(appUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除应用（用户）
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 删除应用（管理员）
     *
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @PostMapping("/delete/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        
        boolean result = appService.deleteAppByAdmin(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取应用详情（用户）
     *
     * @param id      应用ID
     * @param request HTTP请求
     * @return 应用详情
     */
    @GetMapping("/get")
    public BaseResponse<AppVO> getAppById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        AppVO appVO = appService.getAppVOById(id, loginUser.getId());
        return ResultUtils.success(appVO);
    }

    /**
     * 根据ID获取应用详情（管理员）
     *
     * @param id 应用ID
     * @return 应用详情
     */
    @GetMapping("/get/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        
        AppVO appVO = appService.getAppVOByIdByAdmin(id);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页查询应用列表（用户）
     *
     * @param appQueryRequest 查询请求
     * @param request          HTTP请求
     * @return 分页结果
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<AppVO>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 设置分页参数
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        
        Page<AppVO> appVOPage = appService.listAppVOByPage(appQueryRequest, loginUser.getId());
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/list/featured")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        // 设置分页参数
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        
        Page<AppVO> appVOPage = appService.listFeaturedAppVOByPage(appQueryRequest);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        // 设置分页参数
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        
        Page<AppVO> appVOPage = appService.listAppVOByPageByAdmin(appQueryRequest);
        return ResultUtils.success(appVOPage);
    }

    // endregion
}