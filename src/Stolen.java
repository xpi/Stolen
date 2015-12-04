
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Stolen {
    static String downloadPath = "/Volumes/storage/";
    static Map<Integer,Integer> trys = new HashMap<Integer,Integer>();
    public static String[] sto(int id) {
        Document doc;
        try {
            doc = Jsoup.connect(
                    "http://www.xianayi.net/template/default/images/ajax.php?action=geturl&id="
                            + id + "&amp;play_mode=1").get();
            Elements els = doc.select("m");

            if (els.size() <= 0) {
                return null;
            }
            String fullName = els.attr("label");
            String mp3url = els.attr("src");

            if (!mp3url.endsWith(".mp3")) {
                mp3url += ".mp3";
            }
            String filename = mp3url.substring(mp3url.lastIndexOf("/") + 1);
            String prefix1 = "http://a6.xianayi.net/music/";
            String prefix2 = "http://www.xianayi.net/";

            if (mp3url.contains("down2.php")) {
                String[] strs = mp3url.split("down2.php\\?");
                mp3url = strs[0] + strs[1];
            }else if(!mp3url.contains("http://")){
                mp3url=prefix2+mp3url;
                System.out.println("not http");
            } else {
                mp3url = prefix1 + filename;
                System.out.println("not down.php");
            }
            fullName = fullName + mp3url.substring(mp3url.lastIndexOf("."));
            String[] name_url = new String[3];
            name_url[0] = fullName;
            name_url[1] = mp3url;
//            URL mp3 = new URL(mp3url);
//            HttpURLConnection conn = (HttpURLConnection) mp3.openConnection();
//            int mp3len =  conn.getContentLength();
//            if (mp3len<1000){
//                System.out.println("bad file");
//                return null;
//            }
            name_url[2] = null;

            return name_url;
        } catch (SocketTimeoutException ste){
            System.out.println("SocketTimeoutException!!!!!!!"+id);
            return sto(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void download(String fullName, String mp3url) throws Exception {
        URL mp3 = new URL(mp3url);
        HttpURLConnection conn = (HttpURLConnection) mp3.openConnection();
        System.out.println( conn.getContentLength());
        conn.setReadTimeout(2000);
        FileOutputStream fos = new FileOutputStream(downloadPath + fullName);
        InputStream is = conn.getInputStream();
        byte[] buf = new byte[2048000];
        int len = 0;
        int count = 0;

        int fileSzie = conn.getContentLength();
        System.out.println(fileSzie);
        while ((len = is.read(buf)) > 0) {
            count = count + len;
            fos.write(buf, 0, len);
        }
        is.close();
        fos.close();
    }

    public static void main(String[] args) throws Exception {
        new Thread(new Spide(1, 1000)).start();
        new Thread(new Spide(1001, 2000)).start();
        new Thread(new Spide(2001, 3000)).start();
        new Thread(new Spide(3001, 4000)).start();
        new Thread(new Spide(4001, 5000)).start();
        new Thread(new Spide(5001, 6000)).start();
        new Thread(new Spide(6001, 7000)).start();
        new Thread(new Spide(7001, 8000)).start();
        new Thread(new Spide(8001, 8412)).start();

    }


}


class Spide implements Runnable {
    private int from;
    private int end;

    public Spide(int from, int end) {
        this.from = from;
        this.end = end;
    }

    public void run() {

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.2.1:3306/latin_music", "root", "");
            Statement state = conn.createStatement();
            String[] values = null;
            for (int i = from; i <= end; i++) {
                if ((values= Stolen.sto(i))!=null) {
                    PreparedStatement pstate =  conn.prepareStatement("insert into xianayi(id,music_name,url,music_size) values(?,?,?,?)");
                    if(conn.createStatement().executeQuery("select * from xianayi where id = "+i).next()){
                        continue;
                    };
                    pstate.setInt(1, i);
                    pstate.setString(2,values[0]);
                    pstate.setString(3,values[1]);
                    pstate.setString(4,values[2]);
                    pstate.execute();
                    pstate.close();
                }else{
                    System.out.println(i+"bad url");
                }
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}