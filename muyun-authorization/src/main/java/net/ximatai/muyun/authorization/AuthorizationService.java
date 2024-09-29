package net.ximatai.muyun.authorization;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.model.AuthorizedResource;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AuthorizationService implements IAuthorizationService {

    @Inject
    IDatabaseOperationsStd databaseOperations;

    @Override
    public boolean isAuthorized(String userID, String module, String action) {
        if (!"0".equals(userID)) {
            System.out.println(userID);
        }

        return false;
    }

    @Override
    public boolean isDataAuthorized(String userID, String module, String action, String dataID) {
        return false;
    }

    @Override
    public String getAuthCondition(String userID, String module, String action) {
        return "";
    }

    @Override
    public List<String> getAllowedActions(String userID, String module) {
        return List.of();
    }

    @Override
    public List<AuthorizedResource> getAuthorizedResources(String userID, String module) {
        return List.of();
    }

    @Override
    public List<String> getUserAvailableRoles(String userID) {
        List<Map<String, Object>> rows = databaseOperations.query("""
            select id_at_auth_role
            from platform.auth_user_role
            where id_at_auth_user = ?
            and exists(select * from platform.auth_role where auth_role.id = id_at_auth_role)
            """, userID);
        return rows.stream().map(it -> it.get("id_at_auth_role").toString()).toList();
    }
}
