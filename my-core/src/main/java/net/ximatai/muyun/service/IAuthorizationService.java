package net.ximatai.muyun.service;

import net.ximatai.muyun.model.AuthorizedResource;
import net.ximatai.muyun.model.IRuntimeUser;

import java.util.List;

public interface IAuthorizationService {

    /**
     * 校验功能权限
     *
     * @param user
     * @param module
     * @param action
     * @return
     */
    boolean isAuthorized(IRuntimeUser user, String module, String action);

    /**
     * 校验数据权限
     *
     * @param user
     * @param module
     * @param action
     * @param dataID
     * @return
     */
    boolean isDataAuthorized(IRuntimeUser user, String module, String action, String dataID);

    /**
     * 获取权限条件字符串
     *
     * @param user
     * @param module
     * @param action
     * @return
     */
    String getAuthCondition(IRuntimeUser user, String module, String action);

    /**
     * 获取可访问的功能列表
     *
     * @param user
     * @param module
     * @return
     */
    List<String> getAllowedActions(IRuntimeUser user, String module);

    /**
     * 获取可访问的全量资源列表
     *
     * @param user
     * @param module
     * @return
     */
    List<AuthorizedResource> getAuthorizedResources(IRuntimeUser user, String module);

    /**
     * 获取用户拥有的角色信息
     *
     * @param user
     * @return
     */
    List<String> getUserAvailableRoles(IRuntimeUser user);

}
