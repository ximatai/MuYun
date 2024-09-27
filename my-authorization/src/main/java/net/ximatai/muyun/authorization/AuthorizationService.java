package net.ximatai.muyun.authorization;

import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.model.AuthorizedResource;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;

@ApplicationScoped
public class AuthorizationService implements IAuthorizationService {
    @Override
    public boolean isAuthorized(IRuntimeUser user, String module, String action) {
        if (user != null) {
            System.out.println(user.getUsername());
        }

        return false;
    }

    @Override
    public boolean isDataAuthorized(IRuntimeUser user, String module, String action, String dataID) {
        return false;
    }

    @Override
    public String getAuthCondition(IRuntimeUser user, String module, String action) {
        return "";
    }

    @Override
    public List<String> getAllowedActions(IRuntimeUser user, String module) {
        return List.of();
    }

    @Override
    public List<AuthorizedResource> getAuthorizedResources(IRuntimeUser user, String module) {
        return List.of();
    }

    @Override
    public List<String> getUserAvailableRoles(IRuntimeUser user) {
        return List.of();
    }
}
