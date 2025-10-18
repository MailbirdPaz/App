package org.mailbird.Core.Entity;

import lombok.With;

@With
public record User(int id, String email, String name) {
}

//  String passwd, String host, String port
