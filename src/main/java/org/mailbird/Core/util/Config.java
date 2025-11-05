package org.mailbird.Core.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private Dotenv dotenv;
    public String DatabaseUrl;

    public Config() {
        this.dotenv = Dotenv.load();
        this.DatabaseUrl = dotenv.get("DATABASE_URL");
    }

    public String Get(String key) {
        return this.dotenv.get(key);
    }
}
