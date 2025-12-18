package org.mailbird.Core.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.Getter;
import lombok.Setter;
import org.mailbird.Core.DAO.UserDao;
import org.mailbird.Core.domain.entity.UserEntity;
import org.mailbird.Core.domain.model.User;
import org.mailbird.Core.util.Credentials;

import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private UserDao userDao;
    @Setter
    @Getter
    private User user;
    @Getter
    private Credentials credentials;
    public Session session;


    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /// LogInDefault - log in by email and password. It finds / creates user in database and try to connect to the mail server
    /// @return true if success, false otherwise
    public void SetUserCredentials(String email, String password, String host, String port, String protocol) {
        this.credentials = new Credentials(password, host, email, port, protocol);

        // first try to find user in database
        var res = this.userDao.GetByEmail(email);
        if (res.isPresent()) {
            this.user = new User(res.get());
            return;
        }

        // create user
        var newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setPassword(password); // hash it
        newUser.setHost(host);
        newUser.setPort(port);
        this.userDao.Insert(newUser);

        this.user = new User(newUser);
    }

    public void SetUserByEmail(String email) {
        var res = this.userDao.GetByEmail(email);
        if (res.isPresent()) {
            this.user = new User(res.get());
            this.credentials = new Credentials(user.password(), user.host(), user.email(), user.port(), "IMAP");
        }
    }

    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "User";
        }
        return email.substring(0, email.indexOf("@"));
    }

    public ArrayList<User> GetAllUsers() {
        var users = this.userDao.GetList();
        // cast entity to model
        ArrayList<User> result = new ArrayList<>();
        for (var u : users) {
            result.add(new User(u));
        }
        return result;
    }

    public void LogInOAuth2() {

    }
}
