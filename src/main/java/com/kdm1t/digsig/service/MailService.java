package com.kdm1t.digsig.service;

import com.kdm1t.digsig.Tools.FileTools;
import com.kdm1t.digsig.Tools.KeysTools;
import com.kdm1t.digsig.Tools.MailTools;
import com.kdm1t.digsig.rest.entity.EmailMessageResponse;
import com.kdm1t.digsig.rest.entity.EmailStatus;
import com.kdm1t.digsig.rest.entity.SendEmailRequest;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSenderImpl jms;
    private static final String FROM = "kdm1t@internet.ru";

    public void sendEmail(SendEmailRequest request) throws Exception {
        MimeMessage message = jms.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(FROM);
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getMessage());

        FileTools.writeFile(KeysTools.KEYS_PATH + "md5", KeysTools.getEncryptedHash(request.getMessage()));
        helper.addAttachment("md5", new FileSystemResource(new File(KeysTools.KEYS_PATH + "md5")));

        jms.send(message);
    }

    public List<EmailMessageResponse> readInboundEmails() {//create session object

        List<EmailMessageResponse> responses = new ArrayList<>();

        Session session = MailTools.getImapSession();
        try {
            Store store = session.getStore("imap");
            String password = new String(Files.readAllBytes(Path.of("Z:/keys/external_password.txt")));
            store.connect("imap.mail.ru", 993, "kdm1t@internet.ru", password);
            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);//fetch messages
            Message[] messages = inbox.getMessages();//read messages
            for (int i = 0; i < messages.length; i++) {
                Message msg = messages[i];
                MailTools.saveAttachmentsFromMessage(msg, "Z:/keys/Hash/" + i);
                EmailMessageResponse response = new EmailMessageResponse(
                        i,
                        getSenders(msg.getFrom()),
                        msg.getSubject(),
                        MailTools.getTextFromMessage(msg).trim(),
                        //TODO: trim() - Временное решение.
                        // В конце сообщений, состоящих ТОЛЬКО из латиницы, добавляется \r\n при чтении контента
                        msg.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );
                response.setCheckStatus(checkMessage(response.getMessage(), response.getNumber()) ? EmailStatus.OK : EmailStatus.ERROR);
                responses.add(response);
            }

        } catch (Exception e) {
            System.out.println("Exception in reading EMails : " + e.getMessage());
            e.printStackTrace();
        }

        return responses;
    }

    private String getSenders(Address[] addresses) {
        StringBuilder senders = new StringBuilder("[");
        for (Address address : addresses) {
            senders
                    .append(address.toString())
                    .append(";");
        }
        return senders.append("]").toString();
    }

    public boolean checkMessage(String message, int numberOfMessage) throws Exception {
        byte[] decryptedHashBytes = KeysTools.getDecryptedHash(KeysTools.KEYS_PATH + "Hash" + File.separator + numberOfMessage + File.separator + "md5");
        String decryptedHash = new String(decryptedHashBytes);
        System.out.println("Пришедший с письмом хэш: " + decryptedHash);
        String messageHash = DigestUtils.md5Hex(message);
        System.out.println("Хэш сообщения: " + messageHash);
        System.out.printf("Сообщение: [%s]%n", message);
        return Objects.equals(messageHash, decryptedHash);
    }

}
