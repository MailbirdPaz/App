package org.mailbird.Core.Port;

import jakarta.mail.NoSuchProviderException;

import java.util.Properties;

public interface IMail {
    public void Session(Properties props) throws NoSuchProviderException; // TODO: подумать над аргументами
    public void SendMail(); // TODO
    public void LoadMails(); // TODO: args and pagination
    public void LoadMail(int id); // TODO
}
