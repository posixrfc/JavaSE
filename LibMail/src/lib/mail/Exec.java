package lib.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.MimeMessage.RecipientType;

public class Exec
{
	public static void main1(String[] args) throws Exception
	{
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", "smtp.sina.cn");
		props.setProperty("mail.smtp.auth", "true");
		
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		session.setDebug(true);
		final String emailAccount = "q1770750695@", charset = "UTF-8";
		message.setFrom(new InternetAddress(emailAccount + "sina.cn", "user_aa", charset));
		message.setRecipient(RecipientType.TO, new InternetAddress(emailAccount + "yeah.net", "user_bb", charset));
		message.addRecipient(RecipientType.TO, new InternetAddress(emailAccount + "163.com", "user_cc", charset));
		message.setRecipient(RecipientType.CC, new InternetAddress("1770750695@qq.com", "user_dd", charset));
		message.addRecipient(RecipientType.CC, new InternetAddress(emailAccount + "sina.com", "user_ff", charset));
		message.setRecipient(RecipientType.BCC, new InternetAddress(emailAccount + "sohu.com", "user_gg", charset));
		message.addRecipient(RecipientType.BCC, new InternetAddress(emailAccount + "gmail.com", "user_hh", charset));
		message.setSubject("subject_attachment", charset);
		
		MimeBodyPart image = new MimeBodyPart();
		DataHandler dh = new DataHandler(new FileDataSource("收购难过.jpg"));
		image.setDataHandler(dh);
		image.setContentID("image_id");
		
		MimeBodyPart text = new MimeBodyPart();
		text.setContent("this image<br/><img src=\"cid:image_id\"/>", "text/html;charset=UTF-8");
		
		MimeMultipart mm_text_image = new MimeMultipart();
		mm_text_image.addBodyPart(text);
		mm_text_image.addBodyPart(image);
		mm_text_image.setSubType("related");
		
		MimeBodyPart text_image = new MimeBodyPart();
		text_image.setContent(mm_text_image);
		
		MimeBodyPart attachment = new MimeBodyPart();
		DataHandler dh2 = new DataHandler(new FileDataSource("破产法.docx"));
		attachment.setDataHandler(dh2);
		attachment.setFileName(MimeUtility.encodeText(dh2.getName()));
		
		MimeMultipart mm = new MimeMultipart();
		mm.addBodyPart(text_image);
		mm.addBodyPart(attachment);
		mm.setSubType("mixed");
		
		message.setContent(mm);
		
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		df.setCalendar(Calendar.getInstance());
		message.setSentDate(df.parse("19891004132455"));
		message.saveChanges();
		
		Transport transport = session.getTransport();
		transport.connect(emailAccount + "sina.cn", "password");
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
		System.err.println(Integer.toUnsignedString(1400000000, 36));
	}
	public static void main2(String[] args) throws Exception
	{
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", "smtp.sina.com");
		props.setProperty("mail.smtp.auth", "true");
		
//		props.setProperty("mail.smtp.port", "465");
//		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.setProperty("mail.smtp.socketFactory.fallback", "false");
//		props.setProperty("mail.smtp.socketFactory.port", "465");
		
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		session.setDebug(true);
		final String emailAccount = "q1770750695@", charset = "UTF-8";
		message.setFrom(new InternetAddress(emailAccount + "sina.com", "user_aa", charset));
		message.setRecipient(RecipientType.TO, new InternetAddress("1770750695@qq.com", "user_bb", charset));
//		message.addRecipient(RecipientType.TO, new InternetAddress(emailAccount + "163.com", "user_cc", charset));
//		message.setRecipient(RecipientType.CC, new InternetAddress(emailAccount + "sina.cn", "user_dd", charset));
//		message.addRecipient(RecipientType.CC, new InternetAddress(emailAccount + "sina.com", "user_ff", charset));
//		message.setRecipient(RecipientType.BCC, new InternetAddress(emailAccount + "sohu.com", "user_gg", charset));
//		message.addRecipient(RecipientType.BCC, new InternetAddress(emailAccount + "gmail.com", "user_hh", charset));
		message.setContent("<span style=\"color:red\">second</span><br/><a href=\"https://www.baidu.com/\">百度一下</a>", "text/html;charset=UTF-8");
//		DateFormat df = new SimpleDateFormat("yyyyMMdd");
//		df.setCalendar(Calendar.getInstance());
//		message.setSentDate(df.parse("19891004"));
		message.setSentDate(new Date());
		
		Transport transport = session.getTransport();
		transport.connect("q1770750695@sina.com", "666666");
		int i = 64;
		while (i < Integer.MAX_VALUE)
		{
			try
			{
				String subject = "subject_" + Integer.toUnsignedString(i++, 16);
				message.setSubject(subject, charset);
				message.saveChanges();
				transport.sendMessage(message, message.getAllRecipients());
				System.err.println(subject);
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
				i = 0;
				System.err.println("-- exception --");
			}
			finally
			{
				try {
					if (0 != i)
					{
						transport.close();
						System.err.println("-- finally closed --");
						break;
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}
		transport.close();
	}
}