package kz.sabyrzhan.rssnewsfeed;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DBMigration {
    public static void migrate(DataSource dataSource) {
        var flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
    }
}
