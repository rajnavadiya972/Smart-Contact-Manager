package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	public boolean sendEmail(String to,String subject,String text) {
		boolean flag=false;
		
		String from="navadiyaraj333@gmail.com";
		//logic
		//smtp properties
		Properties properties=new Properties();
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		
		String username="navadiyaraj333@gmail.com";
		String password="govxquraikxpoiws";
		//session
		Session session=Session.getInstance(properties,new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,password);
			}
		});
		
		try {
			 
			Message message=new MimeMessage(session);
			
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
//			message.setText(text);
			message.setContent(text,"text/html");
			
			Transport.send(message);
			flag=true;
			
			
		} catch (Exception e) {
			// : handle exception
			e.printStackTrace();
		}
		return flag;
	}
}
