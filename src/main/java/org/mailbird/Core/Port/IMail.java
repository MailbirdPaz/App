package org.mailbird.Core.Port;

import java.util.Properties;

public interface IMail {
    public void ConnectHost(Properties properties);
    public void SetSession(); // TODO: подумать над аргументами
    public void SendMail(); // TODO
    public void LoadMails(); // TODO: args and pagination
    public void LoadMail(int id); // TODO
}
