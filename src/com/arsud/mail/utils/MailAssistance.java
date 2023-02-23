package com.arsud.mail.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

public class MailAssistance {

	// SMTP
	private Properties props;
	private Session session;
	private Message message;
	private MimeBodyPart bodyMain, bodySign;
	private List<MimeBodyPart> listBodyFile;
	private Multipart part;

	private String signature;
	private Properties signProp;

	public MailAssistance() {
		loadVariable();
		loadSignFromFile();
		setSMTP();
	}

	public void loadVariable() {
		String dir = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "settings/Variable.ini";
		signProp = new Properties();
		try {
			signProp.load(new InputStreamReader(new FileInputStream(dir.substring(1, dir.length())), "UTF-8"));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	private void setSMTP() {
		props = new Properties();
		props.put("mail.smtp.host", signProp.getProperty("HOST"));
		props.put("mail.smtp.port", signProp.getProperty("PORT"));
		props.put("mail.smtp.auth", "true");
	}

	private Boolean setMailRecever(RecipientType type, String receiver) {
		if (validateMailAddress(receiver)) {
			try {
				message.addRecipient(type, new InternetAddress(receiver));
			} catch (MessagingException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			return true;
		} else {
			return false;
		}
	}

	private Boolean setFrom() throws UnsupportedEncodingException, MessagingException {
		if (validateMailAddress(signProp.getProperty("ID"))) {
			message.setFrom(
					new InternetAddress(signProp.getProperty("ID"), signProp.getProperty("MAIL_NAME"), "utf-8"));
			return true;
		} else {
			return false;
		}
	}

	private Boolean setSubject(String subject) throws MessagingException {
		if (!subject.equals("")) {
			message.setSubject(subject);
			return true;
		} else {
			return false;
		}
	}

	private void setContents(String contents) throws MessagingException {
		bodyMain = new MimeBodyPart();
		bodyMain.setContent(contents, "text/html; charset=utf-8");
	}

	private void setSign() throws MessagingException {
		bodySign = new MimeBodyPart();
		bodySign.setContent(signature, "text/html; charset=utf-8");
	}

	private void setMultiPart() throws MessagingException {
		part = new MimeMultipart();
		part.addBodyPart(bodyMain);

		if (listBodyFile != null) {
			listBodyFile.stream().forEach(bf -> {
				try {
					part.addBodyPart(bf);
				} catch (MessagingException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			});
		}

		part.addBodyPart(bodySign);
		message.setContent(part);
	}

	public void addFile(String dir) {
		try {
			if (listBodyFile == null) {
				listBodyFile = new ArrayList<MimeBodyPart>();
			}

			MimeBodyPart tempPart = new MimeBodyPart();
			File file = new File(dir);
			tempPart = new MimeBodyPart();
			FileDataSource fds = new FileDataSource(file.getAbsolutePath());
			tempPart.setDataHandler(new DataHandler(fds));
			tempPart.setFileName(fds.getName());
			listBodyFile.add(tempPart);
		} catch (MessagingException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	private Boolean validateMailAddress(String mailAddress) {
		return mailAddress.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");
	}

	public void sendMail(List<String> recever, List<String> cc, String subject, String contents) {

		session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(signProp.getProperty("ID"), signProp.getProperty("PWD"));
			}
		});
		message = new MimeMessage(session);

		try {
			recever.stream().forEach(r -> setMailRecever(Message.RecipientType.TO, r));
			cc.stream().forEach(c -> setMailRecever(Message.RecipientType.CC, c));
			setFrom();
			setSubject(subject);
			setContents(contents.replace("\n\r", "<br>"));
			setSign();
			setMultiPart();
			Transport.send(message);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} finally {
			session = null;
			message = null;
			bodyMain = null;
			bodySign = null;
			listBodyFile = null;
			part = null;
		}
	}

	private void loadSignFromFile() {

		signature = "";
		String dir = (ClassLoader.getSystemClassLoader().getResource(".").getPath() + "settings/signature.txt");
		Path path = Paths.get(dir.substring(1, dir.length()));
		List<String> list = new ArrayList<String>();

		try {
			list = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		for (String readLine : list) {
			signature += replaceSignValue(readLine) + "\r\n";
		}
	}

	private String replaceSignValue(String signValue) {

		String delim = StringUtils.substringBetween(signValue, "%");

		if (delim != null) {
			signValue = signValue.replace("%" + delim + "%", signProp.getProperty(delim));
		}

		return signValue;
	}

	public String getFromMail() {
		return signProp.getProperty("ID");
	}

	public String getDefaultMessage() {

		try {
			String dir = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "settings/DefaultMessage.txt";
			List<String> list = Files.readAllLines(Paths.get(dir.substring(1, dir.length())), StandardCharsets.UTF_8);
			String msg = "";
			for (String readLine : list) {
				msg += replaceSignValue(readLine) + "\n\r";
			}
			return msg;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		return "";
	}
}
