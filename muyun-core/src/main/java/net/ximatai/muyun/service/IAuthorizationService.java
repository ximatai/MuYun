package net.ximatai.muyun.service;

import net.ximatai.muyun.model.AuthorizedResource;

import java.util.List;

public interface IAuthorizationService {

    /**
     * 校验功能权限
     *
     * @param userID
     * @param module
     * @param action
     * @return
     */
    boolean isAuthorized(String userID, String module, String action);

    /**
     * 校验数据权限
     *
     * @param userID
     * @param module
     * @param action
     * @param dataID
     * @return
     */
    boolean isDataAuthorized(String userID, String module, String action, String dataID);

    /**
     * 获取权限条件字符串
     *
     * @param userID
     * @param module
     * @param action
     * @return
     */
    String getAuthCondition(String userID, String module, String action);

    /**
     * 获取可访问的功能列表
     *
     * @param userID
     * @param module
     * @return
     */
    List<String> getAllowedActions(String userID, String module);

    /**
     * 获取可访问的全量资源列表
     *
     * @param userID
     * @param module
     * @return
     */
    List<AuthorizedResource> getAuthorizedResources(String userID, String module);

    /**
     * 获取用户拥有的角色信息
     *
     * @param userID
     * @return
     */
    List<String> getUserAvailableRoles(String userID);

}
