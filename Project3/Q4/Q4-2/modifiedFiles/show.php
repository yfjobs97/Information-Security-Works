<?php
	// Connects to the Database 
	include('connect.php');
	connect(); 
	
	//if the login form is submitted 
	if (!isset($_GET['pid'])) {
		
		if (isset($_GET['delpid'])){
			$pid = mysql_real_escape_string($_GET[delpid]);//Input sanitasation
			mysql_query("DELETE FROM threads WHERE id = '".$pid."'") or die(mysql_error());
		}
			header("Location: members.php");
	}
		?>  
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>hackme</title>
<link href="style.css" rel="stylesheet" type="text/css" media="screen" />
<?php
	include('header.php');
	if(!isset($_COOKIE['hackme'])){
		 die('Why are you not logged in?!');
	}else
	{
		print("<p>Logged in as <a>$_COOKIE[hackme]</a></p>");
	}
?>
<?php
	$threads = mysql_query("SELECT * FROM threads WHERE id = '".$_GET[pid]."'") or die(mysql_error());
	while($thisthread = mysql_fetch_array( $threads )){
?>
	<div class="post">
	<div class="post-bgtop">
	<div class="post-bgbtm">
		<h2 class="title"><a href="show.php?pid=<?php echo $thisthread[id] ?>"><?php echo $thisthread[title]?></a></h2>
							<p class="meta"><span class="date"> <?php echo date('l, d F, Y',$thisthread[date]) ?> - Posted by <a href="#"><?php echo $thisthread[username] ?> </a></p>
         
         <div class="entry">
			
            <?php echo $thisthread[message] ?>
            					
		 </div>
         
	</div>
	</div>
	</div>
    
    <?php
		if ($_COOKIE[hackme] == $thisthread[username])
		{
	?>
    	<a href="show.php?delpid=<?php echo $thisthread[id]?>">DELETE</a>
    <?php
		}
	?> 

<?php
}
	include('footer.php');
?>
</body>
</html>
