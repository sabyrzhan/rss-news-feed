package kz.sabyrzhan.rssnewsfeed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static kz.sabyrzhan.rssnewsfeed.model.Models.User;

@RequiredArgsConstructor
public class UsersRepository {
    private static final UserRowMapper ROW_MAPPER = new UserRowMapper();
    private final JdbcTemplate jdbcTemplate;

    public User findUserByUsername(String username) {
        var usersList = jdbcTemplate.query("select * from users where username = ?", ROW_MAPPER, username);
        return usersList.isEmpty() ? null : usersList.get(0);
    }

    public void save(User user) {
        var query = "insert into users(username, password) values(?,?)";
        jdbcTemplate.update(query, user.username(), user.password());
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            var id = rs.getInt("id");
            var username = rs.getString("username");
            var password = rs.getString("password");
            var user = new User(id, username, password);

            return user;
        }
    }
}
