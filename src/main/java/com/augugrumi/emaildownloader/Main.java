package com.augugrumi.emaildownloader;

import com.sun.mail.imap.IMAPFolder;
import org.jsoup.Jsoup;

import javax.activation.DataHandler;
import javax.mail.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by davide on 19/06/17.
 */
public class Main {

    /**
     * Return the primary text content of the message.
     */
    private static String getText(Part p) throws
            MessagingException, IOException {

        boolean textIsHtml = false;

        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return "";
    }

    public static void main(String[] argv) {

        if (argv.length != 3) {
            System.err.println("Need at least one argument");
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        IMAPFolder folder = null;
        Store store = null;
        try {

            Properties prop = System.getProperties();
            prop.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getDefaultInstance(prop, null);

            store = session.getStore("imaps");
            store.connect("imap.googlemail.com", argv[1], argv[2]);

            folder = (IMAPFolder) store.getFolder("inbox"); // This doesn't work for other email account
            //folder = (IMAPFolder) store.getFolder("inbox"); This works for both email account


            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();

            if (messages.length != 0) {
                System.out.println("Messages");
            } else {
                System.out.println("No Messages");
            }

            final BlockingQueue<Message> selectedEMails = new ArrayBlockingQueue<Message>(messages.length);
            final ArrayList<Future<String>> computedStrings = new ArrayList<>(messages.length);

            for (final Message m : messages) {

                FutureTask<String> newJob = new FutureTask<String>(new Callable<String>() {
                    @Override
                    public String call() throws Exception {

                            if (m.getSubject() != null && m.getSubject().equalsIgnoreCase("test ghio-ca")) {
                                selectedEMails.add(m);

                                Object msgContent = m.getContent();

                                String content = "";
                                    /* Check if content is pure text/html or in parts */
                                if (msgContent instanceof Multipart) {

                                    Multipart multipart = (Multipart) msgContent;
                                    for (int j = 0; j < multipart.getCount(); j++) {

                                        BodyPart bodyPart = multipart.getBodyPart(j);

                                        String disposition = bodyPart.getDisposition();

                                        if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {

                                            DataHandler handler = bodyPart.getDataHandler();

                                        } else {

                                            content = Main.getText(bodyPart);  // the changed code
                                        }
                                    }
                                } else {
                                    content = m.getContent().toString();
                                }

                                content = Jsoup.parse(content).text();
                                char[] tmp = content.toCharArray();

                                StringBuilder formattedContent = new StringBuilder();

                                for (int i = 0; i < tmp.length; i++) {
                                    if ("$".equalsIgnoreCase("" + tmp[i])) {
                                        formattedContent.append("\n");
                                    }

                                    formattedContent.append(tmp[i]);
                                }

                                content = formattedContent.toString();

                                ProcessBuilder builder = new ProcessBuilder("perl", "script.pl", content);
                                builder.redirectErrorStream(true);
                                Process process = builder.start();
                                InputStream is = process.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                                StringBuilder res = new StringBuilder();
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    res.append(line);
                                }

                                //System.out.println(res.toString());
                                //computedStrings.add(res.toString());

                                return res.toString();
                            }

                        return "";
                    }
                });

                computedStrings.add(newJob);
                executor.submit(newJob);


            }

            String s;
            ArrayList<String[]> splittedResult = new ArrayList<>();

            for (Future<String> ft : computedStrings) {
                s = ft.get();

                if (!"".equalsIgnoreCase(s)) {
                    // Split the result
                    String[] res = s.split("\n");
                    splittedResult.add(res);

                }
            }

            int  azure_match = 0;
            int google_match = 0;
            int imagga_match = 0;
            int watson_match = 0;

            int  azure_err = 0;
            int google_err = 0;
            int imagga_err = 0;
            int watson_err = 0;

            int i;
            int tot = 0;
            for (String[] ss: splittedResult) {
                tot++;
                for (String tmps: ss) {
                    String [] res = tmps.split("!");
                    i = 0;
                    for (String val : res) {
                        String[] m_vs_e = val.split(",");
                        switch (i) {
                            case 0:
                                azure_match += Integer.parseInt(m_vs_e[0]);
                                azure_err += Integer.parseInt(m_vs_e[1]);
                                break;
                            case 1:
                                google_match += Integer.parseInt(m_vs_e[0]);
                                google_err += Integer.parseInt(m_vs_e[1]);
                                break;
                            case 2:
                                imagga_match += Integer.parseInt(m_vs_e[0]);
                                imagga_err += Integer.parseInt(m_vs_e[1]);
                                break;
                            case 3:
                                watson_match += Integer.parseInt(m_vs_e[0]);
                                watson_err += Integer.parseInt(m_vs_e[1]);
                                break;
                        }
                        i++;
                    }
                }

            }

            FileWriter writer = new FileWriter("csv.csv");

            CSVUtils.writeLine(writer, Arrays.asList("Service", "match", "error"), ',');
            CSVUtils.writeLine(writer, Arrays.asList("Azure", azure_match + "", azure_err + ""), ',');
            CSVUtils.writeLine(writer, Arrays.asList("Google", google_match + "", google_err + ""), ',');
            CSVUtils.writeLine(writer, Arrays.asList("Imagga", imagga_match + "", imagga_err + ""), ',');
            CSVUtils.writeLine(writer, Arrays.asList("Watson", watson_match + "", watson_err + ""), ',');

            writer.flush();
            writer.close();

            // Rscript --vanilla sillyScript.R
            ProcessBuilder builder = new ProcessBuilder("Rscript", "--vanilla", "graphs.R", argv[0]);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder res2 = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                res2.append(line);
            }

            System.out.println("Output from R:\n" + res2.toString());

            System.out.println("DONE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
