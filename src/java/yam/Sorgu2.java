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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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

public class Sorgu2 {
    String kelimeler;
    String urller;
    
    ArrayList<String> words;
    ArrayList<String> urls;
    
    Locale trlocale = new Locale("tr-TR");

    public Sorgu2() {
        words = new ArrayList<>();
        urls = new ArrayList<>();
        
        kelimeler = null;
        urller = null;
        System.out.println("Yeni sorgu oluşturuldu.");
    }
    
    public String getKelimeler() {
        return kelimeler;
    }

    public void setKelimeler(String kelimeler) {
        this.kelimeler = kelimeler;
    }
    
    public void kelimeleriBol(String kelimeler){
        String[] wordsArr = kelimeler.split(System.getProperty("line.separator"));
        for(String word: wordsArr){
            word = word.trim();
            word = removeSpecial(word);
            words.add(word);
            // System.out.println(word);
        }
    }
    
    public void urlleriBol(String urller){
        String[] urlsArr = urller.split(System.getProperty("line.separator"));
        for(String url: urlsArr){
            urls.add(url);
            // System.out.println(url);
        }
    }

    public String getUrller() {
        return urller;
    }

    public void setUrller(String urller) {
        this.urller = urller;
    }
    
    public ArrayList<Belge> ara() throws IOException{
        try {
            disableSSLCertCheck();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Sorgu2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(Sorgu2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<Belge> belgeler = new ArrayList<>();
        
        // Her bir bağlantıya ayrı ayrı bağlanılıyor.
        for(String url: urls){
            Document doc = Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(true)
                .timeout(0)
                .get();
            
            // Kaynak kodu okunuyor.
            String str = doc.toString().toLowerCase();
            
            // Kaynak kodundan sözcükler alınıyor.
            ArrayList<String> belgeSozcukleri = sozcukleriGetir(str);
            
            System.out.println(belgeSozcukleri);
            
            // Belge oluşturuluyor.
            Belge belge = new Belge(url);
            belge.setTitle(doc.title());
            
            // Her bir kelime için, skor hesaplanacak.
            for(String word: words){
                System.out.println("bakılan word =" + word + "olacak");
                // Kelime geçiş sayılarına sıfır değeriyle ekleniyor.
                belge.getGecisSayilari().put(word, 0);
                
                // Aranan her bir kelime için belgedeki kelimelere bakılacak.
                for(String belgeSozcugu: belgeSozcukleri){
                    // Eğer belgedeki kelime aranan kelimeye eşitse, belge kelimelerinde sayısı bir arttırılıyor.
                    if(word.equals(belgeSozcugu)){
                        // System.out.println(word + " == " + belgeSozcugu);
                        belge.getGecisSayilari().put(word, belge.getGecisSayilari().get(word) + 1);
                    } else{
                        // System.out.println(word + " != " + belgeSozcugu);
                    }
                }
            }
            
            // Belge listeye ekleniyor.
            belgeler.add(belge);
        }
        
        // Belgenin skorunu hesaplamak için internetten bazı araştırmalar yapıldı.
        // Tf-idf denen bir yöntem uygulandı.
        // Formüle göre,
        // Bir sözcüğün ağırlığı: BF x DF x N
        // BF = Sözcüğün belgede geçiş sayısı.
        // DF = ln(Toplam belge sayısı/Sözcüğün bulunduğu belge sayısı).
        // N = (BF x DF) sonuçlarının kare toplamlarının karekökü.
        
        // Listeye eklenen belgeler geziliyor.
        for(Belge belge: belgeler){
            // System.out.println(belge.getUrl() + " için bakılıyor: ");
            // System.out.println(belge.getGecisSayilari());
            
            double wfToplami = 0;
            
            // Kelimeler geziliyor.
            for(String word: words){
                // System.out.println(word + " kelimesi için:");
                
                // Kelimenin geçiş sayısı alınıyor.
                int bf = belge.getGecisSayilari().get(word);
                
                // Kaç belgede geçtiği bulunuyor.
                int sozcuguIcerenBelgeSayisi = 0;
                for(Belge icBelge: belgeler){
                    if(icBelge.getGecisSayilari().get(word) != 0){
                        sozcuguIcerenBelgeSayisi++;
                    }
                }
                
                // double df = Math.log((double)belgeler.size()/(double)sozcuguIcerenBelgeSayisi)+1;
                // System.out.println("belge sayısı = " + belgeler.size());
                // System.out.println("sözcüğü içeren belge sayısı = " + sozcuguIcerenBelgeSayisi);
                // Formül uygulanıyor.
                double wtq = Math.log10((double)belgeler.size()/(double)sozcuguIcerenBelgeSayisi) + 1;
                // System.out.println("wtq = " + wtq);
                if(Double.isInfinite(wtq)) wtq = 0;
                belge.getWtq().put(word, wtq);
                
                double wf = belge.getGecisSayilari().get(word) * belge.getGecisSayilari().get(word);
                // System.out.println("wf = " + wf);
                wfToplami += wf;
            }
            
            // System.out.println("toplam wf = " + wfToplami);
            
            // double normalizasyon = Math.sqrt(sozcukAgirliklariKareToplami);
            
            // Formülde, sonucu hatalı bulduğu düşünüldüğünden bazı değişiklikler yapıldı.
            // Normalizasyon kaldırıldı.
            
            double belgeSkoru = 0;
            for(String word: words){
                // System.out.println(word + " için bakılıyor: ");
                
                /*
                // double score = belge.getSozcukAgirliklari().get(word) * normalizasyon;
                double wordWf = belge.getGecisSayilari().get(word) * belge.getGecisSayilari().get(word);
                System.out.println("wordWf = " + wordWf + ", wfToplami = " + wfToplami);
                double wtd = Math.sqrt(wordWf/wfToplami);
                if(wfToplami == 0){
                    wtd = 0;
                }
                System.out.println("wtd = " + wtd);
                System.out.println("wtq = " + belge.getWtq().get(word) + " idi");
                double score = belge.getWtq().get(word) * wtd;
                */
                double score = belge.getGecisSayilari().get(word) * belge.getWtq().get(word);
                
                belgeSkoru += score;
                belge.getSkor().put(word, score);
                // System.out.println("score of " + word + " is = " + score);
            }
            
            belge.setBelgeSkoru(belgeSkoru);
            
            // System.out.println("belge skoru = " + belgeSkoru);
        }
        
        // Document doc = Jsoup.connect(baglanti).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
        // Document doc = Jsoup.connect(baglanti).get();
        
        // Belgeler skoruna göre sıralanıyor.
        belgeler.sort(Comparator.comparing(Belge::getBelgeSkoru).reversed());
        
        return belgeler;
    }
    
    // Bu fonksiyon Sorgu.java'daki yöntemi içeriyor.
    private ArrayList<String> sozcukleriGetir(String str){
        str = removeHead(str);
        str = removeScript(str);
        str = removeStyle(str);
        str = removeSpecial(str);
        
        int len = str.length();
        char[] k = str.toCharArray();
        
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
        
        ArrayList<Integer> turkishCharacters = new ArrayList<>();
        turkishCharacters.add((int)'ş');
        turkishCharacters.add((int)'ü');
        turkishCharacters.add((int)'ö');
        turkishCharacters.add((int)'İ');
        turkishCharacters.add((int)'ğ');
        turkishCharacters.add((int)'ı');
        turkishCharacters.add((int)'ç');
        
        ArrayList<Integer> digits = new ArrayList<>();
        for(int i = 48; i <= 58; i++){
            digits.add(i);
        }
        
        ArrayList<String> words = new ArrayList<>();
        
        for(int i = 0; i < k.length; i++){
            if(k[i] != ' ' && (int)k[i] != 10 && !digits.contains((int)k[i])){
                // System.out.println("kelime başlıyor, karakter = " + k[i]);
                String word = k[i] + "";
                int j;
                for(j = i + 1; j < k.length; j++){
                    int ascii = (int) k[j];
                    if(ascii == 10) continue;
                    if((ascii >= 97 && ascii <= 122) || turkishCharacters.contains(ascii)){
                        // System.out.println("harf bulundu, kelimeye ekleniyor = " + k[j]);
                        word += k[j];
                        // System.out.println("kelimenin yeni hali = " + word);
                    } else{
                        // System.out.println("kelime bitti, karakter = " + k[j]);
                        word = word.trim();
                        if(!word.equals("&nbsp"))
                            words.add(word.trim());
                        i = j - 1;
                        break;
                    }
                }
            }
        }
        
        return words;
    }
    
    private String removeScript(String content) {
        Pattern p = Pattern.compile("<script[^>]*>(.*?)</script>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    private String removeStyle(String content) {
        Pattern p = Pattern.compile("<style[^>]*>(.*?)</style>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    private String removeHead(String content) {
        Pattern p = Pattern.compile("<head[^>]*>(.*?)</head>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(content).replaceAll("");
    }
    
    private String removeSpecial(String content) {
        // content = Normalizer.normalize(content, Normalizer.Form.NFD);
        // String resultString = content.replaceAll("[^\\x00-\\x7F]", "");
        
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
