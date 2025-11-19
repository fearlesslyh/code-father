package com.lyh.codefather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyh.codefather.model.dto.app.AppAddRequest;
import com.lyh.codefather.model.dto.app.AppQueryRequest;
import com.lyh.codefather.model.dto.app.AppUpdateRequest;
import com.lyh.codefather.model.dto.user.UserLoginRequest;
import com.lyh.codefather.model.vo.LoginUserVO;
import com.lyh.codefather.service.AppService;
import com.lyh.codefather.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用控制器测试
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 测试用户登录
     */
    private LoginUserVO loginUser(String userAccount, String userPassword) throws Exception {
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserAccount(userAccount);
        userLoginRequest.setUserPassword(userPassword);

        String result = mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 解析返回结果获取登录用户信息
        // 这里简化处理，实际应该解析JSON获取data字段
        return new LoginUserVO();
    }

    @Test
    void testAddApp() throws Exception {
        // 先注册并登录用户
        userService.userRegister("testAppController", "12345678", "12345678");
        LoginUserVO loginUserVO = loginUser("testAppController", "12345678");

        // 创建应用请求
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个任务管理应用，支持添加、编辑和删除任务功能");

        // 发送请求
        mockMvc.perform(MockMvcRequestBuilders.post("/app/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appAddRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testUpdateApp() throws Exception {
        // 先注册并登录用户
        userService.userRegister("testUpdateApp", "12345678", "12345678");
        LoginUserVO loginUserVO = loginUser("testUpdateApp", "12345678");

        // 创建应用
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个笔记应用");
        long appId = appService.createApp(appAddRequest, 1L); // 假设用户ID为1

        // 更新应用请求
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(appId);
        appUpdateRequest.setAppName("我的笔记应用");

        // 发送请求
        mockMvc.perform(MockMvcRequestBuilders.post("/app/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testListAppByPage() throws Exception {
        // 先注册并登录用户
        userService.userRegister("testListApp", "12345678", "12345678");
        LoginUserVO loginUserVO = loginUser("testListApp", "12345678");

        // 创建应用查询请求
        AppQueryRequest appQueryRequest = new AppQueryRequest();
        appQueryRequest.setCurrent(1);
        appQueryRequest.setPageSize(10);

        // 发送请求
        mockMvc.perform(MockMvcRequestBuilders.post("/app/list/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appQueryRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testListFeaturedAppByPage() throws Exception {
        // 创建应用查询请求
        AppQueryRequest appQueryRequest = new AppQueryRequest();
        appQueryRequest.setCurrent(1);
        appQueryRequest.setPageSize(10);

        // 发送请求
        mockMvc.perform(MockMvcRequestBuilders.post("/app/list/featured")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appQueryRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}