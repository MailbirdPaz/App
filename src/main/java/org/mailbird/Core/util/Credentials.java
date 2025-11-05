package org.mailbird.Core.util;

import lombok.With;

import java.util.Properties;

@With
public record Credentials(String password, String host, String user, String port, String protocol) {
    public Properties AsProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", this.protocol.toLowerCase());
        props.put(setKey("host"), this.host);
        props.put(setKey("port"), this.port);
        props.put(setKey("ssl.enable"), "true");

        return props;
    }

    public String setKey(String key) {
        return "mail." + this.protocol.toLowerCase() + "." + "key";
    }
}
