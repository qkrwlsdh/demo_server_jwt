package com.example.springjwt.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;  // 타임리프
    @Value("${spring.mail.username}")
    private String id;

    public MimeMessage createMessage(String to, String content, String type) throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        log.info("보내는 내용 : " + content);

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, "UTF-8"); // use multipart (true)
//        mimeMessageHelper.setCc("ykkang@bankedin.io");

        message.addRecipients(MimeMessage.RecipientType.TO, to);    // 보내는 대상
        if (type.equals("id")) {
            message.setSubject("아이디 찾기 결과");
        } else if (type.equals("pw")) {
            message.setSubject("임시 비밀번호 발급 결과");
        }
        message.setText(setContext(content, type), "utf-8", "html");  // 내용, charset타입, subtype
        message.setFrom(new InternetAddress(id,"Bankedin_admin")); //보내는 사람의 메일 주소, 보내는 사람 이름

        return message;
    }

    private String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return type.equals("id") ? templateEngine.process("mail", context) : templateEngine.process("mailPw", context);
    }

    // 인증코드 만들기
    /*public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) { // 인증코드 6자리
            key.append((rnd.nextInt(10)));
        }
        return key.toString();
    }*/

    /*
        메일 발송
        sendSimpleMessage의 매개변수로 들어온 to는 메일를 받을 메일주소
        MimeMessage 객체 안에 내가 전송할 메일의 내용을 담아준다.
        bean으로 등록해둔 javaMailSender 객체를 사용하여 이메일 send
     */
    public String sendSimpleMessage(String to, String content, String type) throws Exception {
        MimeMessage message = createMessage(to, content, type);
        try{
            javaMailSender.send(message); // 메일 발송
        }catch(MailException es){
            log.error("javaMailSender Process Failed", es);
            throw new IllegalArgumentException();
        }
        return content; // 메일로 보냈던 인증 코드를 서버로 리턴
    }
}
