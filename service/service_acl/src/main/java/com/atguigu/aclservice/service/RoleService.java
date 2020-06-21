package com.atguigu.aclservice.service;

import com.atguigu.aclservice.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-20
 */
public interface RoleService extends IService<Role> {

    // 根据用户获取角色数据
    Map<String, Object> findRoleByUserId(String userId);

    // 根据用户分配角色
    void saveUserRoleRelationShip(String userId, String[] roleId);

    List<Role> selectRoleByUserId(String id);
}
