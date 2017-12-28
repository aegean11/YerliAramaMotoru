<%-- 
    Document   : cevap
    Created on : 06.Ara.2017, 17:29:57
    Author     : 
--%>

<%@page import="java.util.Map"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="yam.Belge"%>
<%@page import="java.util.ArrayList"%>
<%@page import="yam.Sorgu2"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="cekirdek2" scope="request" class="yam.Sorgu2" />


<jsp:setProperty name="cekirdek2" property="kelimeler" />
<jsp:setProperty name="cekirdek2" property="urller" />

<%
    // String kelime = cekirdek2.getKelime();
    String kelime = new String(cekirdek2.getKelimeler().getBytes("iso-8859-1"), "UTF-8");
    cekirdek2.kelimeleriBol(kelime);
    String baglanti = new String(cekirdek2.getUrller().getBytes("iso-8859-1"), "UTF-8");
    cekirdek2.urlleriBol(baglanti);

    ArrayList<Belge> sonuc = cekirdek2.ara();
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
        for(Belge belge: sonuc){
        %>
            <li class="sonuc">
                <div class="sonuc_baslik"><a target="_blank" href="<% out.print(belge.getUrl()); %>"><% out.print(belge.getTitle()); %></a></div>
                <div class="sonuc_link"><% out.print(belge.getUrl()); %></div>
                <div class="sonuc_skor">Puan: <% out.print(String.format("%.3f", belge.getBelgeSkoru())); %></div>
                <%
                for(Map.Entry<String, Integer> entry: belge.getGecisSayilari().entrySet()){
                %>
                <div class="sonuc_kelime"><% out.print(entry.getKey()); %>: <% out.print(entry.getValue()); %> kez</div>
                <%
                }
                %>
            </li>
        <%
        }
        %>
    </div>
</body>
</html>
