package net.ximatai.muyun.migration;

public record MigrateStep(
    Integer version,
    MigrateAction action
) {
    public MigrateStep {
        if (version <= 0) {
            throw new IllegalArgumentException("Migration version must be greater than 0");
        }
    }
}
