<%
import hatenahaiku4j.*
import hatenahaiku4j.util.*
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*
%>

<html>
<head>
<title>データストアテストです。</title>
</head>
<body>
<%
	if (!params?.id) {
		request.setAttribute 'message', '引数:idが指定されていません。'
		forward '/error.gtpl'
	}
	String id = params.id
	def status = new Entity('KIND', id)
	status.id = id
	status.aaa = id.toInteger() * 10
	status.bbb = id.toString()
	status.ccc = new Text(id.toString())
	status.flgT = true
	status.flgF = false
	status.save()
%>
    <p>
    	■結果
    </p>
	<table border="1">
		<tr>
			<th>id</th>
			<th>aaa</th>
			<th>bbb</th>
			<th>ccc</th>
			<th>flgT</th>
			<th>flgF</th>
		</tr>
<%
//	def q = new Query(KeyFactory.createKey('KIND', id))
	def q = new Query('KIND')
	def info = datastoreService.prepare(q).asList(withLimit(20).offset(0))
	info.each {
%>
		<tr>
			<td><%= it?.id %> (<%= it?.id?.class %>)</td>
			<td><%= it?.aaa %> (<%= it?.aaa?.class %>)</td>
			<td><%= it?.bbb %> (<%= it?.bbb?.class %>)</td>
			<td><%= it?.ccc?.value %> (<%= it?.ccc?.class %>)</td>
			<td><%= it?.flgT %> (<%= it?.flgT?.class %>)</td>
			<td><%= it?.flgF %> (<%= it?.flgF?.class %>)</td>
		</tr>
<%
	}
%>
	</table>
</body>
</html>
