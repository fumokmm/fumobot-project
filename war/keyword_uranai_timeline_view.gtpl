<% include '/WEB-INF/includes/header.gtpl' %>

<%
	import dao.*
	import static dao.KeywordUranaiTimelineDao.*
%>

<h1>キーワード占いタイムラインビュー</h1>

<%
	def dao = new KeywordUranaiTimelineDao()
	dao.getStatus(5).each { st ->
%>
<table border="1">
	<tr>
		<th>ステータスID</th>
		<td><%= st[STATUS_ID] %></td>
	</tr>
	<tr>
		<th>作成日時</th>
		<td><%= st[CREATED_AT] %></td>
	</tr>
	<tr>
		<th>お気に入られ</th>
		<td><%= st[FAVORITED] %></td>
	</tr>
	<tr>
		<th>返信元ステータスID</th>
		<td><%= st[IN_REPLY_TO_STATUS_ID] %></td>
	</tr>
	<tr>
		<th>返信元ユーザID</th>
		<td><%= st[IN_REPLY_TO_USER_ID] %></td>
	</tr>
	<tr>
		<th>キーワード</th>
		<td><%= st[KEYWORD] %></td>
	</tr>
	<tr>
		<th>ソース</th>
		<td><%= st[SOURCE] %></td>
	</tr>
	<tr>
		<th>投稿内容</th>
		<td><%= st[TEXT].value %></td>
	</tr>
	<tr>
		<th>ユーザID</th>
		<td><%= st[USER_ID] %></td>
	</tr>
</table>
<%
	}
%>

<p>
    Click <a href="datetime.groovy">here</a> to view the current date/time.
</p>

<% include '/WEB-INF/includes/footer.gtpl' %>
