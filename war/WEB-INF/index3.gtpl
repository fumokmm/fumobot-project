<%
import hatenahaiku4j.*
import hatenahaiku4j.util.*
%>

<html>
<head>
<title>Gealykのテストです</title>
</head>
<body>
<%
def xmlHeader = '<?xml version="1.0" encoding="UTF-8"?>'
def apiLight = new HatenaHaikuAPILight()
param = new QueryParameter(page:0, since:null);
def resultXml = HttpUtil.doGet(HatenaHaikuAPILight.URL_PUBLIC_TIMELINE_XML, param, true);
if (!resultXml.startsWith("<?xml")) {
	resultXml = xmlHeader + resultXml;
%>
	<%= resultXml %>足しました。<br/>
<%
}
def result = apiLight.toStatusList(XmlUtil.getRootElement(resultXml));

result.each{
%>
    <p>
    	<%= it.userId %>  
    	<%= it.text %> 
    	<%= HatenaUtil.formatDate(it.createdAt) %>
    </p>
<%
}
%>
    
</body>
</html>
