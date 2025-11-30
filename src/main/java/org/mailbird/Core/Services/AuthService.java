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
        newUser.setProtocol("IMAP"); // default protocol
        newUser.setName(extractNameFromEmail(email)); // cannot get it, so just get from mail
        newUser.setSurname("Surname"); // default surname
        this.userDao.Insert(newUser);

        this.user = new User(newUser);
    }

    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "User";
        }
        return email.substring(0, email.indexOf("@"));
    }

    public void LogInOAuth2() {

    }
}
