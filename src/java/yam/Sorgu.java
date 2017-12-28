package yam;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

public class Sorgu {
    String kelime;
    String baglanti;

    public Sorgu() {
        kelime = null;
        baglanti = null;
        System.out.println("Yeni sorgu oluşturuldu.");
    }
    
    public String getKelime() {
        return kelime;
    }

    // Verilen kelimeyi trim() fonksiyonu ile ve removeSpecial() fonksiyonu ile temizledikten sonra set eden fonksiyon.
    public void setKelime(String kelime) {
        kelime = kelime.trim();
        kelime = removeSpecial(kelime);
        this.kelime = kelime;
    }

    public String getBaglanti() {
        return baglanti;
    }

    public void setBaglanti(String baglanti) {
        this.baglanti = baglanti;
    }
    
    // Field olarak girilen bağlantıya bağlanan ve kelime sayısını bulan fonksiyon.
    public int ara() throws IOException{
        int count = 0;
        
        try {
            disableSSLCertCheck();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Sorgu.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(Sorgu.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Joup ile linke bağlanılıyor.
        Document doc = Jsoup
                .connect(baglanti)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(true)
                .timeout(0)
                .get();
        
        // Document doc = Jsoup.connect(baglanti).get();
        
        /*
        String text = doc.text().toLowerCase();
        System.out.println(text);
        
        String[] words = text.split(" ");
        System.out.println("aranan kelime = " + getKelime());
        for(String word: words){
            System.out.println(word);
            if(word.equals(getKelime())){
                count++;
            }
        }
        */
        
        // Kaynak kodlarında gerekli temizlemeler yapılıyor.
        String str = doc.toString().toLowerCase();
        str = removeHead(str);
        str = removeScript(str);
        str = removeStyle(str);
        str = removeSpecial(str);
        
        int len = str.length();
        
        char[] k = str.toCharArray();
        
        /*
        for(int i = 0; i < len; i++){
            if(k[i] == '<'){
                String kod = "<";
                int j;
                for(j = i + 1; j < len; j++){
                    kod += k[j];
                    if(k[j] == '>'){
                        str = str.replaceAll(kod, "");
                        i = j;
                        break;
                    }
                }
            }
        }
        */
        
        // Html etiketleri temizleniyor.
        // Her bir < görüşte ondan sonra > görene kadar ilerleniyor ve aradaki her şey boşluğa eşitleniyor.
        for(int i = 0; i < len; i++){
            if(k[i] == '<'){
                k[i] = ' ';
                int j;
                for(j = i + 1; j < len; j++){
                    char c = k[j];
                    k[j] = ' ';
                    if(c == '>'){
                        i = j;
                        break;
                    }
                }
            }
        }
        
        // Türkçe karakterleri içeren ArrayList, karakterlere bakarken kullanılacak.
        ArrayList<Integer> turkishCharacters = new ArrayList<>();
        turkishCharacters.add((int)'ş');
        turkishCharacters.add((int)'ü');
        turkishCharacters.add((int)'ö');
        turkishCharacters.add((int)'İ');
        turkishCharacters.add((int)'ğ');
        turkishCharacters.add((int)'ı');
        turkishCharacters.add((int)'ç');
        
        // Rakamların Ascii karşılıkları.
        ArrayList<Integer> digits = new ArrayList<>();
        for(int i = 48; i <= 58; i++){
            digits.add(i);
        }
        
        // Kelimeleri saklayacak olan ArrayList.
        ArrayList<String> words = new ArrayList<>();
        
        // Karakter karakter bakılıyor
        for(int i = 0; i < k.length; i++){
            // Eğer boşluk olmayan, satır sonu olmayan ve rakam olmayan bir karaktere
            // Denk gelindiyse, kelime başlıyor demektir.
            if(k[i] != ' ' && (int)k[i] != 10 && !digits.contains((int)k[i])){
                // System.out.println("kelime başlıyor, karakter = " + k[i]);
                // Kelime biriktiriliyor.
                String word = k[i] + "";
                int j;
                // Kelimenin başlangıcından itibaren bakılıyor.
                for(j = i + 1; j < k.length; j++){
                    int ascii = (int) k[j];
                    // Satır sonu ise bu kelimeyi biriktirme işlemini etkilememeli.
                    if(ascii == 10) continue;
                    
                    // Eğer normal bir kelime karakteri görmüşsek, kelimeye ekleniyor
                    if((ascii >= 97 && ascii <= 122) || turkishCharacters.contains(ascii)){
                        // System.out.println("harf bulundu, kelimeye ekleniyor = " + k[j]);
                        word += k[j];
                        // System.out.println("kelimenin yeni hali = " + word);
                    } else{
                        // System.out.println("kelime bitti, karakter = " + k[j]);
                        // Eğer kelime karakteri olamayacak bir karakter görülmüşse
                        // Kelime biriktirme işlemi bitmiş demektir, listeye ekleniyor.
                        word = word.trim();
                        if(!word.equals("&nbsp"))
                            words.add(word.trim());
                        i = j - 1;
                        break;
                    }
                }
            }
        }
        
        // System.out.println(words);
        
        // Kelimeler içinde kelimemiz aranıyor ve sayılıyor.
        for(String word: words){
            if(word.equals(getKelime())){
                count++;
            }
        }
        
        return count;
    }
    
    // Kaynak kodu içinden script'leri temizleyen fonksiyon.
    private String removeScript(String content) {
        Pattern p = Pattern.compile("<script[^>]*>(.*?)</script>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    // Kaynak kodları içinden style'ları temizleyen fonksiyon.
    private String removeStyle(String content) {
        Pattern p = Pattern.compile("<style[^>]*>(.*?)</style>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    // Kaynak kodları içinden head'i temizleyen fonksiyon.
    private String removeHead(String content) {
        Pattern p = Pattern.compile("<head[^>]*>(.*?)</head>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    // Kaynak kodları içinden özel karakterleri temizleyen fonksiyon.
    private String removeSpecial(String content) {
        return content
                .replaceAll(",", " ")
                .replaceAll("[.]", " ")
                .replaceAll(";", " ")
                .replaceAll("\"", "")
                .replaceAll("[*]", " ")
                .replaceAll("[“]", " ")
                .replaceAll("/", " ")
                .replaceAll("'", "")
                .replaceAll("[(]", "")
                .replaceAll("[)]", "")
                .replace("?", "")
                .replaceAll("î", "i")
                .replaceAll("â", "a")
                .replaceAll("û", "u")
                .replaceAll("ü", "u")
                .replaceAll("ö", "o")
                .replaceAll("ş", "s")
                .replaceAll("ç", "c")
                .replaceAll("ı", "i")
                .replaceAll("ğ", "g")
                .replaceAll("-", "");
    }

    // Joup ile dökümanın başlığını döndüren fonksiyon.
    public String getTitle() throws IOException {
        // Document doc = Jsoup.connect(getBaglanti()).get();
        Document doc = Jsoup.connect(baglanti).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
        return doc.title();
    }
    
    // Bazı sitelerde JSP sertifika hatası verdiğinden, internetten böyle bir çözüm bulmak durumunda kalındı.
    private void disableSSLCertCheck() throws NoSuchAlgorithmException, KeyManagementException {
    // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
