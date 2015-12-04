
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Stolen {
    static String downloadPath = "/Volumes/storage/";

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
            String prefix = "http://a6.xianayi.net/music/";

            if (!mp3url.contains("down2.php")) {
                mp3url = prefix + filename;
            } else {
                String[] strs = mp3url.split("down2.php\\?");
                mp3url = strs[0] + strs[1];
            }
            fullName = fullName + mp3url.substring(mp3url.lastIndexOf("."));
            String[] name_url = new String[2];
            name_url[0] = fullName;
            name_url[1] = mp3url;
            return name_url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void download(String fullName, String mp3url) throws Exception {
        URL mp3 = new URL(mp3url);
        HttpURLConnection conn = (HttpURLConnection) mp3.openConnection();
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
            for (int i = from; i <= end; i++) {
                if (null != Stolen.sto(i)) {
                    PreparedStatement pstate =  conn.prepareStatement("insert into xianayi(id,music_name,url) values(?,?,?)");
                    if(conn.createStatement().executeQuery("select * from xianayi where id = "+i).next()){
                        continue;
                    };
                    pstate.setInt(1,i);
                    pstate.setString(2, Stolen.sto(i)[0]);
                    pstate.setString(3,Stolen.sto(i)[1]);
                    pstate.execute();
                    System.out.println(i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}