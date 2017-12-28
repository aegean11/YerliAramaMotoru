<%-- 
    Document   : cevap
    Created on : 06.Ara.2017, 17:29:57
    Author     : 
--%>

<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="yam.Belge"%>
<%@page import="java.util.ArrayList"%>
<%@page import="yam.Sorgu3"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="cekirdek3" scope="request" class="yam.Sorgu3" />


<jsp:setProperty name="cekirdek3" property="kelimeler" />
<jsp:setProperty name="cekirdek3" property="urller" />

<%
    // String kelime = cekirdek2.getKelime();
    String kelime = new String(cekirdek3.getKelimeler().getBytes("iso-8859-1"), "UTF-8");
    cekirdek3.kelimeleriBol(kelime);
    String baglanti = new String(cekirdek3.getUrller().getBytes("iso-8859-1"), "UTF-8");
    cekirdek3.urlleriBol(baglanti);

    int sinir2 = Integer.parseInt(request.getParameter("derinlik2"));
    cekirdek3.setDerinlik2sinir(sinir2);
    
    int sinir3 = Integer.parseInt(request.getParameter("derinlik3"));
    cekirdek3.setDerinlik3sinir(sinir3);
    
    ArrayList<ArrayList<Belge>> cikti = cekirdek3.ara();
    ArrayList<Belge> sonucTotal = cikti.get(0);
    ArrayList<Belge> sonucZincir = cikti.get(1);
    HashMap<String, String> esAnlamlilar = cekirdek3.getEsAnlamlilar();
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><% out.print(kelime); %> arama sonuçları</title>
    <link rel="stylesheet" type="text/css" href="stil.css" />
</head>
<body>
    <div class="ustbar">
        <div class="logoArama"><img src="images/logo_small.png" /></div>
    </div>

    <div class="sonucSayfasi">
        <%
        for(Belge belge: sonucTotal){
        %>
            <li class="sonuc">
                <div class="sonuc_baslik"><a target="_blank" href="<% out.print(belge.getUrl()); %>"><% out.print(belge.getTitle()); %></a></div>
                <div class="sonuc_link"><% out.print(belge.getUrl()); %></div>
                <div class="sonuc_skor">Puan: <% out.print(String.format("%.3f", belge.getBelgeSkoru())); %></div>
                <%
                for(Map.Entry<String, Integer> entry: belge.getGecisSayilari().entrySet()){
                %>
                <div class="sonuc_kelime">
                    <% if(esAnlamlilar.containsKey(entry.getKey())){ %>
                        <% out.print(entry.getKey()); %>: <% out.print(entry.getValue()); %> kez.
                        <% if(esAnlamlilar.get(entry.getKey()) == null){ %>
                            (Eş anlamlısı bulunamadı)
                        <% } else{ %>
                            (<% out.print(esAnlamlilar.get(entry.getKey())); %>: <% out.print(belge.getGecisSayilari().get(esAnlamlilar.get(entry.getKey()))); %> kez)
                        <% } %>
                    <% } %>
                </div>
                <%
                }
                %>
            </li>
        <%
        }
        %>
    </div>
    
    <div class="ayrac">Ağaç yapısı:</div>
    
    <div class="sonucSayfasi">
        <%
        for(Belge belge: sonucZincir){
        %>
            <li class="sonuc">
                <div class="sonuc_baslik"><a target="_blank" href="<% out.print(belge.getUrl()); %>"><% out.print(belge.getTitle()); %></a></div>
                <div class="sonuc_link"><% out.print(belge.getUrl()); %></div>
            </li>
            
            <%
            for(Belge belge2: belge.getAltBelgeler()){
            %>
                <li class="sonuc2">
                    <div class="sonuc_baslik2"><a target="_blank" href="<% out.print(belge2.getUrl()); %>"><% out.print(belge2.getTitle()); %></a></div>
                    <div class="sonuc_link"><% out.print(belge2.getUrl()); %></div>
                </li>
                
                <%
                for(Belge belge3: belge2.getAltBelgeler()){
                %>
                    <li class="sonuc3">
                        <div class="sonuc_baslik3"><a target="_blank" href="<% out.print(belge3.getUrl()); %>"><% out.print(belge3.getTitle()); %></a></div>
                        <div class="sonuc_link"><% out.print(belge3.getUrl()); %></div>
                    </li>
                <%
                }
                %>
            <%
            }
            %>
        <%
        }
        %>
    </div>
</body>
</html>
