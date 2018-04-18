<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.4/js/bootstrap-select.min.js">
	
</script>
<link rel="stylesheet" type="text/css" href="main.css" />
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.4/css/bootstrap-select.min.css">
<title>Lolinfo</title>
</head>
<body>
	<nav class="navbar navbar-default">
		<div class="container">
			<a class="navbar-brand" href="#">Lolinfo</a>
		</div>
	</nav>

	<!-- https://moksbalindong.deviantart.com/art/League-of-Legends-Free-Icon-625081580 -->
	<img class="centered" alt="Lolinfo" src="images/lolInfo.png"
		height="200" width="200"/>
	<form method="post" action="/Lolinfo/ProfileServlet">
		<div class="form-group">
			<input type="text" name="findPlayer" id="findPlayer"
				class="form-control belowPicture" placeholder="Search for Player...">
			<select name="serverName" class="selectpicker server"
				data-style="btn-warning" data-width="auto">
				<option value="RU">RU</option>
				<option value="KR">KR</option>
				<option value="BR">BR</option>
				<option value="OCE">OCE</option>
				<option value="JP">JP</option>
				<option value="NA" selected>NA</option>
				<option value="EUNE">EUNE</option>
				<option value="EUW">EUW</option>
				<option value="TR">TR</option>
				<option value="LAN">LAN</option>
				<option value="LAS">LA</option>
			</select>
		</div>
		<button type="submit" class="btn btn-primary btn-md searchButton">Search</button>
	</form>
</body>
</html>