package net.ximatai.muyun.migration;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractMigration {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    MigrationController migrationController;

    public abstract List<MigrateStep> getMigrateSteps();

    public abstract String getAlias();

    @PostConstruct
    @Transactional
    public void init() {
        List<MigrateStep> migrateSteps = new ArrayList<>(getMigrateSteps());
        migrateSteps.sort(Comparator.comparingInt(MigrateStep::version));
        HashSet<Integer> set = new HashSet<>(migrateSteps.stream().map(MigrateStep::version).toList());
        if (set.size() != migrateSteps.size()) {
            throw new IllegalStateException("Duplicate migration version found in " + getAlias());
        }

        int beforeVersion = 0;
        for (MigrateStep step : migrateSteps) {
            Map<String, Object> view = migrationController.view(getAlias());
            Integer currentVersion;
            if (view == null) {
                migrationController.create(Map.of(
                    "id", getAlias(),
                    "i_version", 0
                ));
                currentVersion = 0;
                beforeVersion = 0;
            } else {
                currentVersion = (Integer) view.get("i_version");
                beforeVersion = currentVersion;
            }
            if (currentVersion < step.version()) {
                logger.info("Migrating {} to version {}", getAlias(), step.version());
                step.action().migrate();
                migrationController.update(getAlias(), Map.of("i_version", step.version()));
                logger.info("Migrated {} to version {}", getAlias(), step.version());
            }
        }

        int finalVer = migrateSteps.isEmpty() ? 0 : migrateSteps.getLast().version();
        if (beforeVersion == finalVer) {
            logger.info("Do not need migrate {} , current version is {}", getAlias(), beforeVersion);
        } else {
            logger.info("Migrated {} from {} to {}", getAlias(), beforeVersion, finalVer);
        }
    }
}
