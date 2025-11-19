package com.lyh.codefather.service;

import com.lyh.codefather.model.dto.app.AppAddRequest;
import com.lyh.codefather.model.dto.app.AppQueryRequest;
import com.lyh.codefather.model.dto.app.AppUpdateRequest;
import com.lyh.codefather.model.entity.App;
import com.lyh.codefather.model.entity.User;
import com.lyh.codefather.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用服务测试
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since 2025/11/19
 */
@SpringBootTest
class AppServiceTest {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Test
    void testCreateApp() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testAppUser");
        testUser.setUserPassword("12345678");
        testUser.setUserName("测试应用用户");
        long userId = userService.userRegister(testUser.getUserAccount(), testUser.getUserPassword(), testUser.getUserPassword());
        
        // 创建应用
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个任务管理应用，支持添加、编辑和删除任务功能");
        
        long appId = appService.createApp(appAddRequest, userId);
        assertTrue(appId > 0);
        
        // 验证应用是否创建成功
        App app = appService.getById(appId);
        assertNotNull(app);
        assertEquals("创建一个任务管理", app.getAppName()); // 应用名称应该是提示词前12位
        assertEquals(appAddRequest.getInitPrompt(), app.getInitPrompt());
        assertEquals("html", app.getCodeGenType()); // 默认代码生成类型
        assertEquals(Integer.valueOf(0), app.getPriority()); // 默认优先级
        assertEquals(userId, app.getUserId());
    }

    @Test
    void testUpdateApp() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testUpdateUser");
        testUser.setUserPassword("12345678");
        testUser.setUserName("测试更新用户");
        long userId = userService.userRegister(testUser.getUserAccount(), testUser.getUserPassword(), testUser.getUserPassword());
        
        // 创建应用
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个笔记应用");
        long appId = appService.createApp(appAddRequest, userId);
        
        // 更新应用
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(appId);
        appUpdateRequest.setAppName("我的笔记应用");
        
        boolean result = appService.updateApp(appUpdateRequest, userId);
        assertTrue(result);
        
        // 验证应用是否更新成功
        App updatedApp = appService.getById(appId);
        assertNotNull(updatedApp);
        assertEquals("我的笔记应用", updatedApp.getAppName());
    }

    @Test
    void testDeleteApp() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testDeleteUser");
        testUser.setUserPassword("12345678");
        testUser.setUserName("测试删除用户");
        long userId = userService.userRegister(testUser.getUserAccount(), testUser.getUserPassword(), testUser.getUserPassword());
        
        // 创建应用
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个待办事项应用");
        long appId = appService.createApp(appAddRequest, userId);
        
        // 删除应用
        boolean result = appService.deleteApp(appId, userId);
        assertTrue(result);
        
        // 验证应用是否已删除
        App deletedApp = appService.getById(appId);
        assertNull(deletedApp);
    }

    @Test
    void testGetAppVOById() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testGetUser");
        testUser.setUserPassword("12345678");
        testUser.setUserName("测试获取用户");
        long userId = userService.userRegister(testUser.getUserAccount(), testUser.getUserPassword(), testUser.getUserPassword());
        
        // 创建应用
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitPrompt("创建一个日历应用");
        long appId = appService.createApp(appAddRequest, userId);
        
        // 获取应用详情
        AppVO appVO = appService.getAppVOById(appId, userId);
        assertNotNull(appVO);
        assertEquals(appId, appVO.getId());
        assertEquals("创建一个日历应用", appVO.getAppName());
        assertEquals(appAddRequest.getInitPrompt(), appVO.getInitPrompt());
        assertNotNull(appVO.getUser());
        assertEquals(userId, appVO.getUser().getId());
    }

    @Test
    void testListAppByPage() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testListUser");
        testUser.setUserPassword("12345678");
        testUser.setUserName("测试列表用户");
        long userId = userService.userRegister(testUser.getUserAccount(), testUser.getUserPassword(), testUser.getUserPassword());
        
        // 创建多个应用
        for (int i = 0; i < 5; i++) {
            AppAddRequest appAddRequest = new AppAddRequest();
            appAddRequest.setInitPrompt("创建应用" + i);
            appService.createApp(appAddRequest, userId);
        }
        
        // 分页查询应用列表
        AppQueryRequest appQueryRequest = new AppQueryRequest();
        appQueryRequest.setCurrent(1);
        appQueryRequest.setPageSize(3);
        
        Page<AppVO> appVOPage = appService.listAppVOByPage(appQueryRequest, userId);
        assertNotNull(appVOPage);
        assertEquals(3, appVOPage.getRecords().size()); // 每页3条
        assertTrue(appVOPage.getTotal() >= 5); // 至少5条记录
        
        // 验证每个应用VO都包含用户信息
        for (AppVO appVO : appVOPage.getRecords()) {
            assertNotNull(appVO.getUser());
            assertEquals(userId, appVO.getUser().getId());
        }
    }
}