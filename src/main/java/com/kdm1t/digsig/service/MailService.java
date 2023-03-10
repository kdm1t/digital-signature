package com.kdm1t.digsig.service;

import com.kdm1t.digsig.Tools.FileTools;
import com.kdm1t.digsig.Tools.KeysTools;
import com.kdm1t.digsig.Tools.MailTools;
import com.kdm1t.digsig.rest.entity.EmailMessageResponse;
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

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, KeysTools.readPrivateKey());
        System.out.println("HASH:" + DigestUtils.md5Hex(request.getMessage()));
        byte[] hash = DigestUtils.md5Hex(request.getMessage()).getBytes(StandardCharsets.UTF_8);
        byte[] encryptedHash = encryptCipher.doFinal(hash);
        FileTools.writeFile(KeysTools.KEYS_PATH + "md5", encryptedHash);
        helper.addAttachment("md5", new FileSystemResource(new File(KeysTools.KEYS_PATH + "md5")));

//        byte[] hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "md5"));
//        Cipher decryptCipher = Cipher.getInstance("RSA");
//        decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
//        byte[] decryptedHashBytes = decryptCipher.doFinal(hashFromFile);

        jms.send(message);
    }

    public List<EmailMessageResponse> readInboundEmails() {//create session object

        List<EmailMessageResponse> responses = new ArrayList<>();

        Session session = MailTools.getImapSession();
        try {//connect to message store
            Store store = session.getStore("imap");
            String password = new String(Files.readAllBytes(Path.of("Z:/keys/external_password.txt")));
            store.connect("imap.mail.ru", 993, "kdm1t@internet.ru", password);
            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);//fetch messages
            Message[] messages = inbox.getMessages();//read messages
            for (int i = 0; i < messages.length; i++) {
                Message msg = messages[i];
                MailTools.saveAttachmentsFromMessage(msg, "Z:/keys/Hash" + File.separator + i);
                EmailMessageResponse response = new EmailMessageResponse(
                        i,
                        getSenders(msg.getFrom()),
                        msg.getSubject(),
                        MailTools.getTextFromMessage(msg),
                        msg.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );
                if (checkMessage(response.getMessage(), response.getNumber())) {
                    response.setStatusOk();
                } else {
                    response.setStatusError();
                }
                responses.add(response);
            }

//            Cipher decryptCipher = Cipher.getInstance("RSA");
//            decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
//            byte[] hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "Hash" + File.separator + "md5"));
//            byte[] decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
//            System.out.println("Пришедший с письмом хэш: " + new String(decryptedHashBytes));
//            hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "md5"));
//            decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
//            System.out.println("Сгенерированный при отправке хэш: " + new String(decryptedHashBytes));
//            System.out.println("Хэш сообщения: " + DigestUtils.md5Hex(MailTools.getTextFromMessage(messages[0])));
//            System.out.printf("[%s]%n", "Текст моего письма, хэш которого будет закодирован");
//            System.out.printf("[%s]%n", MailTools.getTextFromMessage(messages[0]));
//            System.out.println("Текст моего письма, хэш которого будет закодирован".equals(MailTools.getTextFromMessage(messages[0])));

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
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
        byte[] hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "Hash" + File.separator + numberOfMessage + File.separator + "md5"));
        byte[] decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
        System.out.println("Пришедший с письмом хэш: " + new String(decryptedHashBytes));
        hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "md5"));
        decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
        System.out.println("Сгенерированный при отправке хэш: " + new String(decryptedHashBytes));
        System.out.println("Хэш сообщения: " + DigestUtils.md5Hex(message));
        System.out.printf("[%s]%n", message);
        return DigestUtils.md5Hex(message).equals(new String(decryptedHashBytes));
    }

}
