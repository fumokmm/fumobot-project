<%
import hatenahaiku4j.*
import hatenahaiku4j.util.*
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*
%>

<html>
<head>
<title>キーワードメーカー系データビューワ</title>
</head>
<body>
	<table border="1">
	<caption>KeywordTemplate</caption>
	<tr style="background-color: cyan">
		<th>key</th>
		<th>title</th>
		<th>size</th>
		<th>hash</th>
		<th>uuid</th>
		<th>keyPhraseNum</th>
		<th>countOfUsing</th>
		<th>updatedAt</th>
		<th>createdAt</th>
		<th>userId</th>
	</tr>
<%
//def q = new Query(KeyFactory.createKey('KeywordTemplate', id))
def q = new Query('KeywordTemplate')
def entityList = datastoreService.prepare(q).asList(withLimit(1000).offset(0))
entityList.each {
%>
<tr style="background-color: lightcyan">
	<td><%= it?.key?.name %></td>
	<td><%= it?.title %></td>
	<td><%= it?.size %></td>
	<td><%= it?.hash %></td>
	<td><%= it?.uuid %></td>
	<td><%= it?.keyPhraseNum %></td>
	<td><%= it?.countOfUsing %></td>
	<td><%= it?.updatedAt %></td>
	<td><%= it?.createdAt %></td>
	<td><%= it?.userId %></td>
</tr>
<%
}
%>
	</table>

<br/>

	<table border="1">
	<caption>KeywordKeyPhrase</caption>
	<tr style="background-color: yellow">
		<th>key</th>
		<th>title</th>
		<th>size</th>
		<th>hash</th>
		<th>uuid</th>
		<th>countOfUsing</th>
		<th>updatedAt</th>
		<th>createdAt</th>
		<th>userId</th>
	</tr>
<%
//def q = new Query(KeyFactory.createKey('KeywordKeyPhrase', id))
q = new Query('KeywordKeyPhrase')
entityList = datastoreService.prepare(q).asList(withLimit(1000).offset(0))
entityList.each {
%>
<tr style="background-color: ivory">
	<td><%= it?.key?.name %></td>
	<td><%= it?.title %></td>
	<td><%= it?.size %></td>
	<td><%= it?.hash %></td>
	<td><%= it?.uuid %></td>
	<td><%= it?.countOfUsing %></td>
	<td><%= it?.updatedAt %></td>
	<td><%= it?.createdAt %></td>
	<td><%= it?.userId %></td>
</tr>
<%
}
%>
	</table>

</body>
</html>
