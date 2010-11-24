<% include '/WEB-INF/includes/header.gtpl' %>

<h1>error</h1>
<p>
<%= request.getAttribute('message') %>
</p>
<% include '/WEB-INF/includes/footer.gtpl' %>
