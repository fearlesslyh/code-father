package com.lyh.codefather.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateMyProfileRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 当前密码（用于验证身份）
     */
    private String currentPassword;

    /**
     * 新密码（可选，不填写则不修改密码）
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkNewPassword;

    private static final long serialVersionUID = 1L;
}