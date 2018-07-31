package com.sap.scp.srch.sif.swa.reporting.readers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sap.scp.srch.sif.swa.reporting.processors.NotificationObject;
import com.sap.scp.srch.sif.swa.reporting.processors.Utils;

public class JavaMailSender {
    Properties javaMailProperties = new Properties();
    String toEmailAddress = "simona.doneva@sap.com;";
    // // S.a. http://www.oracle.com/technetwork/java/javamail-1-149769.pdf
    // // Appendix A.
    Properties p;
    ArrayList < NotificationObject > criticalFieldsList = new ArrayList < NotificationObject > ();

    public JavaMailSender() {
        p = javaMailProperties;
    }

    public void addMailEntry(NotificationObject entry) {
        if (criticalFieldsList == null) {
            criticalFieldsList = new ArrayList < NotificationObject > ();
        }
        criticalFieldsList.add(entry);
    }

    public void sendNotification(boolean isProd) {
        Utils.info("Prod= " + isProd);
        if (!criticalFieldsList.isEmpty()) {
            File fileQA = new File("/opt/clients/swa.reporting/logs/alertLogQA.txt");
            File filePROD = new File("/opt/clients/swa.reporting/logs/alertLogPROD.txt");
            File relevantFile = isProd ? filePROD : fileQA;

            String line = null;
            FileReader fr;
            BufferedReader br;

            if (relevantFile.exists()) {
                try {
                    Utils.info("Checking if alert is necessary by reading existing alert file.");
                    fr = new FileReader(relevantFile);
                    br = new BufferedReader(fr);
                    line = br.readLine();

                    if (line != null && line.contains("ALERT")) {
                        Utils.info("Content read from Alert Info file: " + line);

                        // Code to send out Emails
                        Utils.info("Trying to send E-mail with JavaMailSender.\n ");
                        p.put("mail.host", "<host example>");
                        p.put("mail.encoding", "UTF-8");
                        p.put("mail.from", "notification@sample.com");

                        p.put("proxySet", "true");
                        p.put("socksProxyPort", "8080");
                        p.put("socksProxyHost", "proxy");

                        Utils.info("Set proxy: proxy and port: 8080");

                        p.put("mail.smtp.auth", "false");
                        p.put("mail.smtp.timeout", "30000");

                        Session session = Session.getDefaultInstance(javaMailProperties, null);
                        MimeMessage message = new MimeMessage(session);

                        StringBuffer body = new StringBuffer();
                        body.append("Dear receiver,\n\nPlease be aware of the following:\n\n ");

                        for (NotificationObject obj: criticalFieldsList) {
                            body.append("For table " + obj.getTable() + " in repository " + obj.getRepo() +
                                " a delta of " + obj.getDelta() + " for field " + obj.getField() +
                                " can be observed.\n\n");
                        }
                        body.append(
                            "This might indicate poor system health and it should be investigated whether HANA is loosing data.\n");
                        body.append("Please make sure, that corresponding measures were taken.\n\n");
                        body.append("Thank you.");

                        if (javaMailProperties.getProperty("mail.from") != null) {
                            try {
                                message.setFrom(new InternetAddress(javaMailProperties.getProperty("mail.from")));

                                String[] group = toEmailAddress.split(";");
                                boolean hasTo = false;
                                for (String mailto: group) {
                                    try {
                                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailto));
                                        hasTo = true;
                                        Utils.info("Added receiver: " + mailto);
                                    } catch (Exception e) {
                                        Utils.error("Can not add e-mail recipient: " + mailto + "\r\n" +
                                            e.getStackTrace());
                                    }
                                }

                                if (hasTo) {
                                    String subject = isProd ? " PROD" : " QA";
                                    message.setSubject("Index Statistics Alert" + subject);
                                    message.setContent(body.toString(), "text/plain");
                                    Transport.send(message);
                                    Utils.info("Notification mails were sent out.\n");
                                }
                            } catch (AddressException e) {
                                e.printStackTrace();
                            } catch (MessagingException e) {

                                e.printStackTrace();
                            }
                        }
                        // Once Email were sent, no need to send alert again
                        // in the next Job run
                        Utils.info("Alert Emails were sent out, alert file will be reset.");
                        PrintWriter writer = new PrintWriter(relevantFile);
                        writer.print("");
                        writer.close();

                    } else {
                        Utils.info("Writing Alert indicator for next job.");
                        try (BufferedWriter bw = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(relevantFile)));) {
                            bw.write("ALERT");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                Utils.info("New alert information file was created.");
                try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(relevantFile)));) {
                    bw.write("ALERT");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

        }
    }

}

}
}
}