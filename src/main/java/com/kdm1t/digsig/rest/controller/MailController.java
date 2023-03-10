package com.kdm1t.digsig.rest.controller;

import com.kdm1t.digsig.Tools.FileTools;
import com.kdm1t.digsig.Tools.KeysTools;
import com.kdm1t.digsig.Tools.MailTools;
import com.kdm1t.digsig.rest.entity.SendMailRequest;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final JavaMailSenderImpl jms;

    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    private SendMailRequest send(@RequestBody SendMailRequest request) throws Exception {

//        System.out.println("JMS configuration : " + jms);
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("kdm1t@internet.ru");
//        message.setTo(request.getTo());
//        message.setSubject(request.getSubject());
//        message.setText(request.getText());
//
//        System.out.println("Message sending: " + message);
//        System.out.println("Text from message hashCode = " + DigestUtils.md5Hex(message.getText()));
//        System.out.println("Text from request hashCode = " + DigestUtils.md5Hex(request.getText()));
//
//        String secretMessage = request.getText();
//        Cipher encryptCipher = Cipher.getInstance("RSA");
//        encryptCipher.init(Cipher.ENCRYPT_MODE, KeysTools.readPrivateKey());
//        byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
//        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
//        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
//        message.setText(encodedMessage);
//        jms.send(message);
//
//        Cipher decryptCipher = Cipher.getInstance("RSA");
//        decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
//        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
//        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
//        request.setTo(decryptedMessage);

        MimeMessage message = jms.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("kdm1t@internet.ru");
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getText());

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, KeysTools.readPrivateKey());
        System.out.println("HASH:" + DigestUtils.md5Hex(request.getText()));
        byte[] hash = DigestUtils.md5Hex(request.getText()).getBytes(StandardCharsets.UTF_8);
        byte[] encryptedHash = encryptCipher.doFinal(hash);
        FileTools.writeFile(KeysTools.KEYS_PATH + "md5", encryptedHash);
        helper.addAttachment("Hash", new FileSystemResource(new File(KeysTools.KEYS_PATH + "md5")));

        byte[] hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "md5"));
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
        byte[] decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
        request.setResult(new String(decryptedHashBytes));

        jms.send(message);
        return request;
    }

    @GetMapping(value = "/get_messages", produces = MediaType.APPLICATION_JSON_VALUE)
    private String getMessages() {
        readInboundEmails();
        return "test";
    }

    public void readInboundEmails() {//create session object
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
                MailTools.saveAttachmentsFromMessage(msg, "Z:/keys/Hash");

            }

            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, KeysTools.readPublicKey());
            byte[] hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "Hash" + File.separator + "md5"));
            byte[] decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
            System.out.println("Пришедший с письмом хэш: " + new String(decryptedHashBytes));
            hashFromFile = Files.readAllBytes(Path.of(KeysTools.KEYS_PATH + "md5"));
            decryptedHashBytes = decryptCipher.doFinal(hashFromFile);
            System.out.println("Сгенерированный при отправке хэш: " + new String(decryptedHashBytes));
            System.out.println("Хэш сообщения: " + DigestUtils.md5Hex(MailTools.getTextFromMessage(messages[0])));
            System.out.printf("[%s]%n", "Текст моего письма, хэш которого будет закодирован");
            System.out.printf("[%s]%n", MailTools.getTextFromMessage(messages[0]));
            System.out.println("Текст моего письма, хэш которого будет закодирован".equals(MailTools.getTextFromMessage(messages[0])));

        } catch (Exception e) {
            System.out.println("Exception in reading EMails : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
