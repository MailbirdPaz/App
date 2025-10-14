package org.mailbird.Adapter;

import org.mailbird.Core.Port.IMail;

import java.util.Properties;

// Using POP3 adapter assumes using SMTP.
public class POP3 implements IMail {

    @Override
    public void ConnectHost(Properties properties) {

    }

    @Override
    public void SetSession() {

    }

    @Override
    public void SendMail() {

    }

    @Override
    public void LoadMails() {

    }

    @Override
    public void LoadMail(int id) {

    }
}
