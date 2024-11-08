package net.ximatai.muyun.platform;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

public abstract class ScaffoldForPlatform extends BaseScaffold {

    private final LoadingCache<String, Map<String, ?>> myCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(this::view);

    @Override
    public String getSchemaName() {
        return DB_SCHEMA;
    }

    @Override
    protected void afterInit() {
        super.afterInit();
        if (this instanceof IModuleRegisterAbility ability) {
            ability.registerModule();
        }
    }

    public String idToName(String id) {
        Map<String, ?> user = myCache.get(id);
        if (user != null) {
            return (String) user.get("v_name");
        }
        return null;
    }
}
