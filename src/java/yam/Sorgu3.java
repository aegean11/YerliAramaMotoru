package yam;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

public class Sorgu3 {
    String kelimeler;
    String urller;
    
    ArrayList<String> words;
    ArrayList<String> urls;
    HashMap<String, String> esAnlamlilar;
    
    ArrayList<String> depth2Urls;
    ArrayList<String> depth3Urls;
    
    int derinlik2sinir;
    int derinlik3sinir;

    public Sorgu3() {
        words = new ArrayList<>();
        urls = new ArrayList<>();
        depth2Urls = new ArrayList<>();
        depth3Urls = new ArrayList<>();
        esAnlamlilar = new HashMap<>();
        
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
    
    public void kelimeleriBol(String kelimeler) throws IOException{
        String[] wordsArr = kelimeler.split(System.getProperty("line.separator"));
        for(String word: wordsArr){
            word = word.toLowerCase();
            word = word.trim();
            String esAnlamli = esAnlamliyiGetir(word);
            word = removeSpecial(word);
            if(esAnlamli != null)
                esAnlamli = removeSpecial(esAnlamli);
            words.add(word);
            // System.out.println("kelime -> " + word + ", eş anlamlısı -> " + esAnlamli);
            esAnlamlilar.put(word, esAnlamli);
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
    
    // İki adım derine inerek tüm belgeleri toplayan fonksiyon.
    public ArrayList<ArrayList<Belge>> ara() throws IOException, URISyntaxException{
        // System.out.println("2 sınırı = " + getDerinlik2sinir());
        // System.out.println("3 sınırı = " + getDerinlik3sinir());
        try {
            disableSSLCertCheck();
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(Sorgu3.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Belgelerin ağaç şeklinde listelenebilmesi için ArrayList.
        ArrayList<Belge> belgelerAgac = new ArrayList<>();
        
        // Tüm belgeleri içerecek olan ArrayList.
        ArrayList<Belge> belgeler = new ArrayList<>();
        
        // Tüm bağlantıları içerecek olan ArrayList.
        ArrayList<String> totalUrls = new ArrayList<>();
        
        // Bağlantılar geziliyor.
        for(String url: urls){
            // Bağlantı temizleniyor.
            url = urlTemizle(url);
            
            // Listeye ekleniyor.
            totalUrls.add(url);
            
            // Belge oluşturuluyor.
            Belge belge = getBelge(url);
            
            // Belgelere ekleniyor.
            belgeler.add(belge);
            
            // System.out.println("bakılacak 2 derinlikli yerler:");
            // System.out.println("url = " + url);
            // System.out.println(belge.getLinkedPages());
            
            // 2 Derinlikli belgeleri tutacak olan ArrayList.
            ArrayList<Belge> depth2Pages = new ArrayList<>();
            
            // Heroku'daki sınır değeri için derinlik sayılacak.
            int count2 = 0;
            
            // Belgedeki bağlantılar gezilecek.
            for(String depth2Url: belge.getLinkedPages()){
                
                // 2 Derinlik sınır değeri için kontrol.
                count2++;
                if(count2 > getDerinlik2sinir()){
                    break;
                }
                
                // Bağlantı temizleniyor.
                depth2Url = urlTemizle(depth2Url);
                
                // Eğer listede yoksa
                if(!totalUrls.contains(depth2Url) && !isFile(depth2Url)){
                    // Belge alınıyor ve ekleniyor.
                    Belge depth2Page = getBelge(depth2Url);
                    belgeler.add(depth2Page);
                    // System.out.println(depth2Page.getUrl() + " tamamlandı");
                    depth2Pages.add(depth2Page);
                    totalUrls.add(depth2Url);
                }
            }
            
            // Belgenin alt belgeleri hesaplandığından, atanıyor.
            belge.setAltBelgeler(depth2Pages);
            
            // 2 derinlikli belgeler gezilecek.
            for(Belge dept2Page: depth2Pages){
                // 3 Derinlikli belgeleri tutacak olan ArrayList.
                ArrayList<Belge> depth3Pages = new ArrayList<>();
                
                // System.out.println("bakılacak 3 derinlikli yerler:");
                // System.out.println(dept2Page.getLinkedPages());
                
                // Sınır değeri.
                int count3 = 0;
                
                // 2 Derinlikli belgede yer alan her bir bağlantı gezilecek.
                for(String depth3Url: dept2Page.getLinkedPages()){
                    
                    // Sınır kontrolü.
                    count3++;
                    if(count3 > getDerinlik3sinir()){
                        break;
                    }
                    
                    // Bağlantı temizleniyor ve uygunsa ekleniyor.
                    depth3Url = urlTemizle(depth3Url);
                    if(!totalUrls.contains(depth3Url) && !isFile(depth3Url)){
                        Belge depth3Page = getBelge(depth3Url);
                        belgeler.add(depth3Page);
                        // System.out.println(depth3Page.getUrl() + " tamamlandı");
                        depth3Pages.add(depth3Page);
                        totalUrls.add(depth3Url);
                    }
                }
                
                // Belgeler tamamlanınca 3. derinlikli olarak atanıyor.
                dept2Page.setAltBelgeler(depth3Pages);
            }
            
            // Ana belge ağaca ekleniyor.
            belgelerAgac.add(belge);
        }
        
        // Tüm belgeler için, Sorgu2.java içinde uygulanan skor hesabı tekrar uygulanıyor.
        for(Belge belge: belgeler){
            // System.out.println(belge.getUrl() + " için bakılıyor: ");
            // System.out.println(belge.getGecisSayilari());
            
            double wfToplami = 0;
            for(String word: words){
                // System.out.println(word + " kelimesi için:");
                int bf = 0;
                if(esAnlamlilar.get(word) != null){
                    bf = belge.getGecisSayilari().get(word) + belge.getGecisSayilari().get(esAnlamlilar.get(word));
                } else{
                    bf = belge.getGecisSayilari().get(word);
                }
                
                int sozcuguIcerenBelgeSayisi = 0;
                for(Belge icBelge: belgeler){
                    if(icBelge.getGecisSayilari().get(word) != 0 || (esAnlamlilar.get(word) != null && icBelge.getGecisSayilari().get(esAnlamlilar.get(word)) != 0)){
                        sozcuguIcerenBelgeSayisi++;
                    }
                }
                
                // double df = Math.log((double)belgeler.size()/(double)sozcuguIcerenBelgeSayisi)+1;
                // System.out.println("belge sayısı = " + belgeler.size());
                // System.out.println("sözcüğü içeren belge sayısı = " + sozcuguIcerenBelgeSayisi);
                double wtq = Math.log10((double)belgeler.size()/(double)sozcuguIcerenBelgeSayisi) + 1;
                // System.out.println("wtq = " + wtq);
                if(Double.isInfinite(wtq)) wtq = 0;
                belge.getWtq().put(word, wtq);
                double wf = 0;
                if(esAnlamlilar.get(word) != null){
                    wf = Math.pow(belge.getGecisSayilari().get(word) + belge.getGecisSayilari().get(esAnlamlilar.get(word)), 2);
                } else{
                    wf = Math.pow(belge.getGecisSayilari().get(word), 2);
                }
                // double wf = (belge.getGecisSayilari().get(word)) * (belge.getGecisSayilari().get(word));
                // System.out.println("wf = " + wf);
                wfToplami += wf;
            }
            
            // System.out.println("toplam wf = " + wfToplami);
            
            // double normalizasyon = Math.sqrt(sozcukAgirliklariKareToplami);
            
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
                
                double score = 0;
                
                if(esAnlamlilar.get(word) != null){
                    score = (belge.getGecisSayilari().get(word) + belge.getGecisSayilari().get(esAnlamlilar.get(word))) * belge.getWtq().get(word);
                } else{
                    score = belge.getGecisSayilari().get(word) * belge.getWtq().get(word);
                }
                
                belgeSkoru += score;
                belge.getSkor().put(word, score);
                // System.out.println("score of " + word + " is = " + score);
            }
            
            belge.setBelgeSkoru(belgeSkoru);
            
            // System.out.println("belge skoru = " + belgeSkoru);
        }
        
        // Ve tekrar belgeler sıralanıyor.
        belgeler.sort(Comparator.comparing(Belge::getBelgeSkoru).reversed());
        
        // Sonuç içinde iki ArrayList barındıran bir ArrayList olarak döndürülecek.
        ArrayList<ArrayList<Belge>> sonuc = new ArrayList<>();
        sonuc.add(belgeler);
        sonuc.add(belgelerAgac);
        
        return sonuc;
    }
    
    // İşleri kolaylaştırmak için yazılmış, bağlantıya gidip belgeyi getiren fonksiyon.
    private Belge getBelge(String url) throws IOException, URISyntaxException{
        try {
            disableSSLCertCheck();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("sertifika hatası");
        } catch (KeyManagementException ex) {
            System.out.println("sertifika hatası");
        }
        
        // Bağlanılıyor.
        Document doc = Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(true)
                .timeout(0)
                .get();
        
        String str = doc.toString().toLowerCase();
        
        // System.out.println(str);
        
        // Sözcükler ve başlık atanıyor.
        Belge belge = new Belge(url);
        belge.setTitle(doc.title());
        belge.setWords(sozcukleriGetir(str));
        
        // Bağlantıları tutacak olan ArrayList.
        ArrayList<String> linkedPages = new ArrayList<>();
        
        // A etiketi seçiliyor.
        Elements links = doc.select("a[href]");

        // Bağlantı sonuna yan çizgi konarak alan adı oluşturuluyor.
        String domain = url + "/";
        
        // Bu substring işlemi
        // http://google.com.tr/asdasd/bbb/c için, http://google.com.tr döndürecektir.
        // 0 ile 8. indisten sonraki yan çizginin indisi arası alınıyor.
        domain = domain.substring(0, domain.indexOf('/', 8));
        
        // URI uri = new URI(domain);
        // Http ve https temizleniyor.
        String domainWithoutHttp = domain.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
        
        // Bağlantılar gezilecek.
        for (Element link : links){
            
            // Asıl bağlantı adresi alınıyor.
            String href = link.attr("abs:href");
            // System.out.println("href = " + href);
            // System.out.println("domainWithoutHttp = " + domainWithoutHttp);
            
            // Domain'i içeriyorsa, ve daha önce eklenmemişse ekleniyor. 
            if(href.contains(domainWithoutHttp)){
                if(!linkedPages.contains(href)){
                    linkedPages.add(href);
                }
            // Domaini içermiyorsa.
            } else{
                if(!href.isEmpty()){
                    // İçerisinde nokta yoksa, site içi bağlantısıdır.
                    if(!href.contains(".")){
                        // Başında yan çizgi ile birlikte listeye ekleniyor.
                        if(href.charAt(0) != '/'){
                            href = "/" + href;
                        }

                        String newLink = domain + href;
                        if(!linkedPages.contains(newLink)){
                            linkedPages.add(newLink);
                        }
                    // İçinde nokta varsa, harici bağlantıdır.
                    } else{
                        // Uludağ Sözlük sayfasında karşılaşılan bir problem.
                        // Bağlantı başında iki yan çizgi içerdiğinden hata alınıyordu.
                        // Bağlantı, başında / içermeyene dek yan çizgiler temizleniyor.
                        while(href.startsWith("/")){
                            href = href.substring(1, href.length());
                        }

                        /*
                        while(href.endsWith("/")){
                            href = href.substring(0, href.length() - 1);
                        }
                        */

                        // Ve ekleniyor.
                        if(!linkedPages.contains(href)){
                            linkedPages.add(href);
                        }
                    }
                }
            }
        }
        
        // Belgenin bağlantıları atanıyor.
        belge.setLinkedPages(linkedPages);
        
        // Kelimeler gezilecek.
        for(String word: words){
            belge.getGecisSayilari().put(word, 0);
            
            // Eğer eş anlamlısı varsa, eş anlamlısı da listeye ekleniyor.
            if(esAnlamlilar.get(word) != null)
                belge.getGecisSayilari().put(esAnlamlilar.get(word), 0);
            
            // Aranan her bir kelime için belgedeki kelimelere bakılacak.
            for(String belgeSozcugu: belge.getWords()){
                // Eğer belgedeki kelime aranan kelimeye eşitse, belge kelimelerinde sayısı bir artıyor.
                if(word.equals(belgeSozcugu)){
                    belge.getGecisSayilari().put(word, belge.getGecisSayilari().get(word) + 1);
                } else if(esAnlamlilar.get(word) != null){
                    if(esAnlamlilar.get(word).equals(belgeSozcugu)){
                        belge.getGecisSayilari().put(esAnlamlilar.get(word), belge.getGecisSayilari().get(esAnlamlilar.get(word)) + 1);
                    }
                }
            }
        }
        
        return belge;
    }
    
    // Bu fonksiyon, dersimiz.com'a bağlanıp eş anlamlı kelimeyi getiriyor.
    private String esAnlamliyiGetir(String kelime) throws IOException{
        try {
            disableSSLCertCheck();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("sertifika hatası");
        } catch (KeyManagementException ex) {
            System.out.println("sertifika hatası");
        }
        
        String url = "https://www.dersimiz.com/yakin-ve-es-anlamli-anlamdas-kelimeler-sozlugu.asp?q=" + kelime;
        
        Document doc = Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(true)
                .timeout(0)
                .get();
        
        Elements esElement = doc.select("p[style=background:#FFF65E] > strong");
        if(esElement.size() > 1){
            String es = esElement.get(1).text().toLowerCase();
            if(es.contains(",")){
                return removeSpecial(es.split(",")[0]);
            } else{
                return removeSpecial(es);
            }
        } else{
            return null;
        }
    }
    
    // Bağlantıdaki gereksiz karakterleri temizleyen fonksiyon.
    private String urlTemizle(String url){
        if(url.charAt(url.length()-1) == '/'){
            url = url.substring(0, url.length()-1);
        }
        
        char[] karakterler = url.toCharArray();
        for(int i = karakterler.length - 1; i >= 0; i--){
            if(karakterler[i] == '/' || karakterler[i] == '.'){
                return url;
            } else if(karakterler[i] == '#'){
                return url.substring(0, i);
            }
        }
        
        return url;
    }
    
    // Dosya olup olmadığını kontrol eden fonksiyon.
    private boolean isFile(String url){
        char[] karakterler = url.toCharArray();
        int i;
        for(i = karakterler.length - 1; i >= 0; i--){
            if(karakterler[i] == '.'){
                break;
            }
        }
        
        String expectedExtension = url.substring(i+1, url.length());
        
        ArrayList<String> fileExtensions = new ArrayList<>();
        fileExtensions.add("gif");
        fileExtensions.add("jpg");
        fileExtensions.add("png");
        fileExtensions.add("cgi");
        fileExtensions.add("js");
        fileExtensions.add("java");
        fileExtensions.add("class");
        fileExtensions.add("mp4");
        fileExtensions.add("mp3");
        
        if(fileExtensions.contains(expectedExtension)){
            return true;
        } else{
            return false;
        }
    }
   
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

    public int getDerinlik2sinir() {
        return derinlik2sinir;
    }

    public void setDerinlik2sinir(int derinlik2sinir) {
        this.derinlik2sinir = derinlik2sinir;
    }

    public int getDerinlik3sinir() {
        return derinlik3sinir;
    }

    public void setDerinlik3sinir(int derinlik3sinir) {
        this.derinlik3sinir = derinlik3sinir;
    }

    public HashMap<String, String> getEsAnlamlilar() {
        return esAnlamlilar;
    }

    public void setEsAnlamlilar(HashMap<String, String> esAnlamlilar) {
        this.esAnlamlilar = esAnlamlilar;
    }
}
