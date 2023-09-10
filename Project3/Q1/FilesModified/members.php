<?php
	// Connects to the Database 
	include('connect.php');
	connect();
	
	//if the login form is submitted 
	if (isset($_POST['submit'])) {
		
		$_POST['username'] = trim($_POST['username']);
		if(!$_POST['username'] | !$_POST['password']) {
			die('<p>You did not fill in a required field.
			Please go back and try again!</p>');
		}
		
		$passwordHash = hash('sha256', $_POST['password']);
		
		$check = mysql_query("SELECT * FROM users WHERE username = '".$_POST['username']."' AND pass = '". $passwordHash. "'")or die(mysql_error());//Q1-1 fix
		
 		//Gives error if user already exist
 		$check2 = mysql_num_rows($check);
		if ($check2 == 0) {
			die("<p>Sorry, user name and password does not match.</p>");
		}
		else
		{
			$hour = time() + 3600;
			$ciphering = "AES-128-CTR";			
			$encryption = openssl_encrypt($_POST['username'], $ciphering, $passwordHash, 0, $passwordHash);
			setcookie(hackme, $encryption, $hour); 
			setcookie(hackme_pass, $passwordHash, $hour);
			header("Location: members.php");
		}
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
?>
<div class="post">
	<div class="post-bgtop">
		<div class="post-bgbtm">
        <h2 class = "title">hackme bulletin board</h2>
        	<?php
            if(!isset($_COOKIE['hackme']) || !isset($_COOKIE['hackme_pass'])){//Check whether both cookies are present Q1-2 fix
				 die('Why are you not logged in?!');
			}else
			{
				$ciphering = "AES-128-CTR";			
				$decryptUsername = openssl_decrypt($_COOKIE['hackme'], $ciphering, $_COOKIE['hackme_pass'], 0, $_COOKIE['hackme_pass']);
				$reVerify = mysql_query("SELECT * FROM users WHERE username = '". $decryptUsername."' AND pass = '". $_COOKIE['hackme_pass']. "'")or die(mysql_error());//Revefify cookies
				$reCheck2 = mysql_num_rows($reVerify);
				if ($reCheck2 == 0) {
					die("<p>Sorry, user name and password does not match.</p>");
				}
				else
				{
					print("<p>Logged in as <a>$decryptUsername</a></p>");
				}

			}
			?>
        </div>
    </div>
</div>

<?php
	$threads = mysql_query("SELECT * FROM threads ORDER BY date DESC")or die(mysql_error());
	while($thisthread = mysql_fetch_array( $threads )){
?>
	<div class="post">
	<div class="post-bgtop">
	<div class="post-bgbtm">
		<h2 class="title"><a href="show.php?pid=<?php echo $thisthread['id'] ?>"><?php echo $thisthread['title']?></a></h2>
							<p class="meta"><span class="date"> <?php echo date('l, d F, Y',$thisthread[date]) ?> - Posted by <a href="#"><?php echo $thisthread[username] ?> </a></p>

	</div>
	</div>
	</div> 

<?php
}
	include('footer.php');
?>
</body>
</html>
