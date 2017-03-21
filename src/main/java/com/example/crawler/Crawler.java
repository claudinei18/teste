package com.example.crawler;


/**
 * Created by claudinei on 21/02/17.
 */

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.jsoup.Jsoup;

import java.net.*;
import java.util.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class Crawler {
    private static Map<InetAddress, String> hmapDns;
    static Mongo mongo;
    static DB db;
    static DBCollection collection;

    /**
     * @param dominio
     * @return
     */
    public static InetAddress getIp(String dominio) {
        try {
            InetAddress ip = InetAddress.getByName(new URL(dominio).getHost());
            String insertIp = ip.getHostAddress();
            insertInMongo(dominio, insertIp);

            return ip;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getHtmlContent(String dominio) {
        String content = null;
        URLConnection connection = null;
        try {
            URL url = new URL(dominio);
            connection = url.openConnection();
            if(isHtml(dominio)){
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");

                content = scanner.next();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return content;
    }

    public static List getLinksFromPage(String page) throws IOException {
        List link = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(page);
        while(m.find()) {
            if( (!m.group(1).endsWith(".png"))  &&
                (!m.group(1).endsWith(".jpeg")) &&
                (!m.group(1).endsWith(".jpg"))  &&
                (!m.group(1).endsWith(".js")) ){
                System.out.println(m.group(1));
                link.add(m.group(1));
            }
        }
        return link;
    }

    public static boolean isHtml(String dominio) throws IOException {
        boolean resp = false;
        URL url = new URL(dominio);
        URLConnection c = url.openConnection();
        String contentType = c.getContentType();
        System.out.println(dominio + " -> CONTENT-TYPE: " + contentType);

        if(contentType.startsWith("text/html")){
            resp = true;
        }

        return resp;
    }

    public static List<String> parserRobots(String url){
        List<String> allows = new ArrayList<String>();


        return allows;
    }

    public static void insertInMongo(String url, String dominio){
        BasicDBObject document = new BasicDBObject();

        BasicDBObject documentDetail = new BasicDBObject();
        documentDetail.put("url", url);
        documentDetail.put("dominio", dominio);

        document.put("detail", documentDetail);

        collection.insert(document);

    }


    /**
     * @param args
     * */
    public static void main(String[] args) {
        hmapDns = new HashMap<InetAddress, String>();
        mongo = new Mongo("localhost", 27017);
        db = mongo.getDB("test");
        collection = db.getCollection("dns");

        BasicDBObject document = new BasicDBObject();
        document.put("database", "mkyongDB");
        document.put("table", "dns");

        collection.insert(document);

        try {
            String read = null;
            BufferedReader brDomain = new BufferedReader( new FileReader(System.getProperty("user.dir") + "/files/domainslist/domains.txt") );

            BufferedWriter bwBlackDomain = new BufferedWriter( new FileWriter(System.getProperty("user.dir") + "/files/domainslist/black_domains.txt") );

            BufferedWriter bwIp = new BufferedWriter( new FileWriter(System.getProperty("user.dir") + "/files//cachedns/ips.txt") );
            BufferedWriter bwHtmlContent = new BufferedWriter( new FileWriter(System.getProperty("user.dir") + "/files/pages/file.txt") );

            while ((read = brDomain.readLine()) != null) {
                String html = getHtmlContent(read);
                if(html == null){
                    System.out.println("DEU ERRO!");
                    bwBlackDomain.write(read);
                }else{
                    List links = getLinksFromPage(html);
//
                    String text = Jsoup.parse(html).text();

                    bwHtmlContent.write(text + "\n");

                    hmapDns.put(getIp(read), read);

                    bwIp.write(getIp(read).getHostAddress() + " " + read + "\n");
                }
            }
            brDomain.close();
            bwIp.close();
            bwHtmlContent.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}