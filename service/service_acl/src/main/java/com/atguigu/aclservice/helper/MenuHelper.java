package com.atguigu.aclservice.helper;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.aclservice.entity.Permission;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 根据权限数据构建登录用户左侧菜单数据
 * </p>
 *
 * @author zli78122
 * @since 2020-06-20
 */
public class MenuHelper {

    /**
     * 构建菜单
     */
    public static List<JSONObject> build(List<Permission> treeNodes) {
        List<JSONObject> menus = new ArrayList<>();
        if (treeNodes.size() == 1) {
            Permission topNode = treeNodes.get(0);
            // 左侧一级菜单
            List<Permission> oneMenuList = topNode.getChildren();
            for (Permission one : oneMenuList) {
                JSONObject oneMenu = new JSONObject();
                oneMenu.put("path", one.getPath());
                oneMenu.put("component", one.getComponent());
                oneMenu.put("redirect", "noredirect");
                oneMenu.put("name", "name_" + one.getId());
                oneMenu.put("hidden", false);

                JSONObject oneMeta = new JSONObject();
                oneMeta.put("title", one.getName());
                oneMeta.put("icon", one.getIcon());
                oneMenu.put("meta", oneMeta);

                List<JSONObject> children = new ArrayList<>();
                List<Permission> twoMeunList = one.getChildren();
                for (Permission two : twoMeunList) {
                    JSONObject twoMeun = new JSONObject();
                    twoMeun.put("path", two.getPath());
                    twoMeun.put("component", two.getComponent());
                    twoMeun.put("name", "name_" + two.getId());
                    twoMeun.put("hidden", false);

                    JSONObject twoMeta = new JSONObject();
                    twoMeta.put("title", two.getName());
                    twoMeun.put("meta", twoMeta);

                    children.add(twoMeun);

                    List<Permission> threeMeunList = two.getChildren();
                    for (Permission three : threeMeunList) {
                        if (StringUtils.isEmpty(three.getPath())) continue;

                        JSONObject threeMeun = new JSONObject();
                        threeMeun.put("path", three.getPath());
                        threeMeun.put("component", three.getComponent());
                        threeMeun.put("name", "name_" + three.getId());
                        threeMeun.put("hidden", true);

                        JSONObject threeMeta = new JSONObject();
                        threeMeta.put("title", three.getName());
                        threeMeun.put("meta", threeMeta);

                        children.add(threeMeun);
                    }
                }
                oneMenu.put("children", children);
                menus.add(oneMenu);
            }
        }
        return menus;
    }
}
