package org.mailbird.Core.Entity;

public record Mail(int id, User from, User to, String subject, String body) {

}
