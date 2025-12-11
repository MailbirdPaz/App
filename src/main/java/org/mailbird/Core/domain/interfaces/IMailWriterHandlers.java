package org.mailbird.Core.domain.interfaces;

public interface IMailWriterHandlers {
    void onSend(String to, String subject, String body);
    void onClose();
}
