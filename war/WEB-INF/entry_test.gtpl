<%
import hatenahaiku4j.*
import hatenahaiku4j.util.*
%>

<html>
<head>
<title>Gealykのエントリテストです</title>
</head>
<body>
<%
def apiAuth = new HatenaHaikuAPI(new LoginUser('fumoinfoboard', 'xxxxx'))
apiAuth.entry('Entry ふろむ　GAE').with{
%>
    <p>
    	■<a href="<%= it.link %>">結果</a>  
    </p>
<%
}
%>
    
</body>
</html>
