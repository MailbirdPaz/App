package org.mailbird.Core.Services;

import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.Getter;
import lombok.Setter;
import org.mailbird.Adapter.POP3;
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


    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /// LogInDefault - log in by email and password. It finds / creates user in database and try to connect to the mail server
    /// @return true if success, false otherwise
    public boolean LogInDefault(String email, String password, String host, String port, String protocol) {
        this.credentials = new Credentials(password, host, email, port, protocol);

        System.out.println("Trying to connect to mail service");
        try {
            POP3 ms =  new POP3(); // mail service
            Session session = ms.NewSession(this.credentials.AsProperties());
            Store store = ms.Connect(session, this.credentials);
            store.close();
        } catch (Exception e) {
            System.out.println("Failed to connect to mail service: " + e.getMessage());
            return false;
        }

        // first try to find user in database
        var res = this.userDao.GetByEmail(email);
        if (res.isPresent()) {
            this.user = new User(res.get());
            return true;
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
        return true;
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
