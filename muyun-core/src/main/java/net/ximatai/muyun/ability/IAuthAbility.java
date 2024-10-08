package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;

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

}
