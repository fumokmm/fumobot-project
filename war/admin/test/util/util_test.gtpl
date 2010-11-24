<%
import util.TextUtil
%>

<html>
<head>
<title>ユーティリティのテストです。</title>
</head>
<body>
<%
	def str1 = '''

		お返事、
		　どうも
		 　　ありがとう
		
	'''

	def str2 = '''
		※
		お返事、
		　どうも
		 　　ありがとう
		
	'''

   boolean resultThanks1 = TextUtil.checkThanks(str1)
   boolean resultIgnore1 = TextUtil.checkIgnore(str1)

   boolean resultThanks2 = TextUtil.checkThanks(str2)
   boolean resultIgnore2 = TextUtil.checkIgnore(str2)
%>
<p>
	■調べた文字
</p>
<p>
	<%= str1 %>
</p>
<p>
	■結果
</p>
<p>
	checkThanks: <%= resultThanks1 %>
	resultIgnore: <%= resultIgnore1 %>
</p>

<hr/>

<p>
■調べた文字
</p>
<p>
<%= str2 %>
</p>
<p>
■結果
</p>
<p>
checkThanks: <%= resultThanks2 %>
resultIgnore: <%= resultIgnore2 %>
</p>
</body>
</html>
