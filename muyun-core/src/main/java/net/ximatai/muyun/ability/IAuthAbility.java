package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 参与权限管理的能力
 */
public interface IAuthAbility extends IRuntimeAbility {

    IAuthorizationService getAuthorizationService();

    @GET
    @Path("/actions")
    default List<String> actions() {
        IAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return List.of();
        }

        String userID = getUser().getId();
        String module = getApiRequest().getModule();

        return authorizationService.getAllowedActions(userID, module);
    }

    @GET
    @Path("/actions/{id}")
    default List<String> actions(@PathParam("id") String id) {
        IAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return List.of();
        }

        String userID = getUser().getId();
        String module = getApiRequest().getModule();

        return authorizationService.getAllowedActions(userID, module)
            .stream().filter(it -> authorizationService.isDataAuthorized(userID, module, it, id))
            .collect(Collectors.toList());
    }

}
