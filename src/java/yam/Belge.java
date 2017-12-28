package yam;

import java.util.ArrayList;
import java.util.HashMap;

// Bu sınıf web site bilgilerini ayrı ayrı tutacak.
public class Belge {
    private String url;
    private HashMap<String, Integer> gecisSayilari;
    private HashMap<String, Double> sozcukAgirliklari;
    private HashMap<String, Double> skor;
    private double belgeSkoru;
    private String title;
    
    private HashMap<String, Double> wtq;
    private HashMap<String, Double> wtd;
    
    private ArrayList<String> words;
    
    private int derinlik;
    
    private ArrayList<String> linkedPages;
    private ArrayList<Belge> altBelgeler;

    public Belge(String url) {
        this.url = url;
        gecisSayilari = new HashMap<>();
        sozcukAgirliklari = new HashMap<>();
        wtq = new HashMap<>();
        wtd = new HashMap<>();
        skor = new HashMap<>();
        words = new ArrayList<>();
        
        linkedPages = new ArrayList<>();
        altBelgeler = new ArrayList<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Integer> getGecisSayilari() {
        return gecisSayilari;
    }

    public void setGecisSayilari(HashMap<String, Integer> gecisSayilari) {
        this.gecisSayilari = gecisSayilari;
    }

    public HashMap<String, Double> getSozcukAgirliklari() {
        return sozcukAgirliklari;
    }

    public void setSozcukAgirliklari(HashMap<String, Double> sozcukAgirliklari) {
        this.sozcukAgirliklari = sozcukAgirliklari;
    }

    public HashMap<String, Double> getSkor() {
        return skor;
    }

    public void setSkor(HashMap<String, Double> skor) {
        this.skor = skor;
    }

    public double getBelgeSkoru() {
        return belgeSkoru;
    }

    public void setBelgeSkoru(double belgeSkoru) {
        this.belgeSkoru = belgeSkoru;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashMap<String, Double> getWtq() {
        return wtq;
    }

    public void setWtq(HashMap<String, Double> wtq) {
        this.wtq = wtq;
    }

    public HashMap<String, Double> getWtd() {
        return wtd;
    }

    public void setWtd(HashMap<String, Double> wtd) {
        this.wtd = wtd;
    }

    public int getDerinlik() {
        return derinlik;
    }

    public void setDerinlik(int derinlik) {
        this.derinlik = derinlik;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }

    public ArrayList<String> getLinkedPages() {
        return linkedPages;
    }

    public void setLinkedPages(ArrayList<String> linkedPages) {
        this.linkedPages = linkedPages;
    }

    public ArrayList<Belge> getAltBelgeler() {
        return altBelgeler;
    }

    public void setAltBelgeler(ArrayList<Belge> altBelgeler) {
        this.altBelgeler = altBelgeler;
    }
}
