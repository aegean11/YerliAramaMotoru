<%-- 
    Document   : cevap
    Created on : 06.Ara.2017, 17:29:57
    Author     : 
--%>

<%@page import="yam.Sorgu"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="cekirdek" scope="request" class="yam.Sorgu" />

<jsp:setProperty name="cekirdek" property="kelime" />
<jsp:setProperty name="cekirdek" property="baglanti" />

<%
    // String kelime = cekirdek.getKelime();
    String kelime = new String(cekirdek.getKelime().getBytes("iso-8859-1"), "UTF-8");
    cekirdek.setKelime(kelime);
    String baglanti = new String(cekirdek.getBaglanti().getBytes("iso-8859-1"), "UTF-8");
    cekirdek.setBaglanti(baglanti);
    String title = cekirdek.getTitle();

    int count = cekirdek.ara();
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
        <li class="sonuc">
            <div class="sonuc_baslik"><a target="_blank" href="<% out.print(baglanti); %>"><% out.print(title); %></a></div>
            <div class="sonuc_link"><% out.print(baglanti); %></div>
            <div class="kelime_sayisi">"<% out.print(kelime); %>" kelimesi sayfada <% out.print(count); %> kez geçiyor.</div>
        </li>
    </div>
</body>
</html>
