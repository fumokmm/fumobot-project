<%
import hatenahaiku4j.*
import hatenahaiku4j.util.*

response.contentType = 'text/html;charset=utf-8'
%>

<html>
<head>
<title>ひとりごとタイムライン</title>
</head>
<body>
<%
def api = new HatenaHaikuAPI(new LoginUser('your_hatena_id', 'your_entry_password'))
def hitorigoto = api.getKeyword('ひとりごと')
%>
<h1>ひとりごとタイムライン</h1>
<%
hitorigoto.api.timeline.each{
%>
    <table style="width:100%; border: solid 1px lightgray; margin-bottom:3px;">
    	<tr>
    		<td rowspan="3">
    			<a href="<%= it.user.entriesUrl %>">
    				<img style="border: 0px; width:64px; height:64px;"
    					src="<%= it.user.profileImageUrl %>">
    			</a>
    		</td>
    		<td style="color:blue; font-weight:bold">
    			<a href="<%= hitorigoto.link %>">
    				<%= it.keyword %>
    			</a>
    		</td>
    		<td style="color:gold; text-align:left;"><%
    			if (it.favorited >= 11) {
		    		%>★<%= it.favorited %>★<%
    			} else {
    				it.favorited.times{ %>★<% }
				}
    		%></td>
    	</tr>
    	<tr>
    		<td colspan="2"><%= it.text %></td>
    	</tr>
    	<tr>
    		<td colspan="2" style="text-align:left;">
    			<span style="color:green">by <a href="<%= it.user.entriesUrl %>"></a>
    			<%= it.user.name %></a>(<%= it.user.followersCount %>)</span>
    			<span style="color:blue">at <a href="<%= it.link %>"><%= it.createdAtString %></a></span>
    			<span style="color:maroon">from <%= it.source %></span>
    		</td>
    	</tr>
	</table>
<%
}
%>
<br/>
<hr>
powerd by <a href="http://d.hatena.ne.jp/fumokmm/20090905/1252143947">HatenaHaiku4J</a> with Groovy and Gaelyk.
</body>
</html>
