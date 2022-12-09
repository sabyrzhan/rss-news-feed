package kz.sabyrzhan.rssnewsfeed;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DBMigration {
    public static void migrate(DataSource dataSource, String location) {
        if (location == null) {
            location = "db/migration";
        }

        var flyway = Flyway
                .configure()
                .locations(location)
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }
}
