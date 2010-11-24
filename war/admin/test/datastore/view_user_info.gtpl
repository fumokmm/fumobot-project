<%
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*
import hatenahaiku4j.util.*
%>

<html>
<head>
<title>ユーザインフォビューワ</title>
</head>
<body>
	<table border="1">
	<tr>
		<th>ユーザ名</th>
		<th>userId</th>
		<th>countOfKeywordUranai</th>
		<th>countOfKeywordMaker</th>
		<th>updatedAt</th>
	</tr>
<%
def q
if (params?.userId) {
	q = new Query(KeyFactory.createKey('UserInfo', params.userId))
} else {
	q = new Query('UserInfo')
}
def info = datastoreService.prepare(q).asList(withLimit(1000).offset(0))
info.each {
%>
<tr>
	<td><%= HatenaUtil.getUserName(it?.key?.name) ?: it?.key?.name %></td>
	<td><%= it?.key?.name %> (<%= it?.key?.name?.class %>)</td>
	<td><%= it?.countOfKeywordUranai %> (<%= it?.countOfKeywordUranai?.class %>)</td>
	<td><%= it?.countOfKeywordMaker %> (<%= it?.countOfKeywordMaker?.class %>)</td>
	<td><%= it?.updatedAt %> (<%= it?.updatedAt %>)</td>
</tr>
<%
}
%>
</table>
</body>
</html>
