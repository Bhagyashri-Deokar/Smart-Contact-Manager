package com.example.demo.Mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService 
{
@Autowired
private JavaMailSender mailSender;
public void sendMail(String toString,String subject,String Body)
{
try
{
SimpleMailMessage message=new SimpleMailMessage();
message.setFrom("deokarbhagyashri1999@gmail.com");
message.setTo(toString);
message.setText(Body);
message.setSubject(subject);
mailSender.send(message);
System.out.println("Mail send successFully");
}catch (Exception e) 
{
System.out.println("Exception="+e);
}
}
}
