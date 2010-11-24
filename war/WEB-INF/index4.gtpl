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
def apiLight = new HatenaHaikuAPILight()
def result = apiLight.getPublicTimeline();
result.each{
%>
    <p>
    	■<%= it.userId %>  
    	■<%= it.text %> 
    	■<%= HatenaUtil.formatDate(it.createdAt) %>
    </p>
<%
}
%>
    
</body>
</html>
