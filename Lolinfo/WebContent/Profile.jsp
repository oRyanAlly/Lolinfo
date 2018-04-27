<%@page import="java.text.DecimalFormat"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="lolinfo.ItemsList"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="java.util.List"%>
<%@page
	import="net.rithms.riot.api.endpoints.match.dto.ParticipantStats"%>
<%@page import="net.rithms.riot.api.endpoints.match.dto.Participant"%>
<%@page
	import="net.rithms.riot.api.endpoints.static_data.dto.SummonerSpell"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
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

	<div class="bgContainer"
		style=" background: linear-gradient(rgba(255,255,255,.4),rgba(255,255,255,.4)), 
			 url(<%String sWallpaper = (String) request.getAttribute("setBackground");
			out.println(sWallpaper);%>)">
		<div class="container userInfo">
			<div class="profilePicture">
				<img class="ppRound" width="128px" height="128px"
					src="<%String sProfilePicture = (String) request.getAttribute("summonerPicture");
			out.println(sProfilePicture);%>">
				<p class="h1 userName">
					<%
						String sName = (String) request.getAttribute("summonerName");
						out.println(sName);
					%>
				</p>
			</div>
			<%
				String sPicTier = (String) request.getAttribute("sPicTier");
				String sTier = (String) request.getAttribute("sTier");
				int nLeaguePoints = (int) request.getAttribute("nLeaguePoints");
				int nWins = (int) request.getAttribute("nWins");
				int nLoses = (int) request.getAttribute("nLoses");
				double dWinRatio = nWins;
				dWinRatio = Math.round(dWinRatio / (nWins + nLoses) * 100);
				String sLeagueName = (String) request.getAttribute("sLeagueName");

				if (sPicTier != null) {
					out.println("<div class='rankStatistics'>");
					out.println("<img src=" + sPicTier + ">");
					out.println("<p class='rankInfo1'>" + sTier + ' ' + request.getAttribute("sRank") + "</p>");
					out.println("<p class='rankInfo2'>" + nLeaguePoints + "LP / " + nWins + "W " + nLoses + "L" + "</p>");
					out.println("<p class='rankInfo3'> Win Ratio " + dWinRatio + "%" + "</p>");
					out.println("<p class='rankInfo3'>" + sLeagueName + "</p>");

					out.println("</div>");
				} else {
					out.println("<div class='hidden'>");
					out.println("<img src=" + sTier + ">");
					out.println("</div>");
				}
			%>
		</div>
		<div class="container rankedChampions">
		<h4 class="s8Title">Champions Played (Season 8)</h4>
		<table class="table">
		<tbody>
			<%List<Integer[]> lChampions = (List<Integer[]>) request.getAttribute("rankChamps");
			ArrayList<Participant> arParticipants = (ArrayList<Participant>) request.getAttribute("S8Stats");
			ArrayList<String> arChampKeyPics = (ArrayList<String>) request.getAttribute("champKeyPics");
			int nIncrement = 0;
			List<Integer[]> lKDAPerChamp = new ArrayList<Integer[]>();
			List<Integer[]> lChampWinLose = new ArrayList<Integer[]>();
			List<Double> lKDA = new ArrayList<Double>();
			List<Double> lTotalCS = new ArrayList<Double>();
			List<Integer> lWinRates = new ArrayList<Integer>();
			for(Integer[] champion : lChampions) {
				int nKills = 0;
				int nAssists = 0;
				int nDeaths = 0;
				int nWinsRank = 0;
				int nLossesRank = 0;
				double dTotalCS = 0;
				int nWinRate = 0;
				int nTotalGames = 0;
				for(Participant participant : arParticipants) {
					if(participant.getChampionId() == champion[0]) {
						ParticipantStats ptStats = participant.getStats();
						nKills = nKills + ptStats.getKills();
						nAssists = nAssists + ptStats.getAssists();
						nDeaths = nDeaths + ptStats.getDeaths();
						if(ptStats.isWin()) {
							nWinsRank = nWinsRank  + 1;
						}
						else {
							nLossesRank = nLossesRank + 1;
						}
						dTotalCS = dTotalCS + ptStats.getTotalMinionsKilled() + ptStats.getNeutralMinionsKilled();
					}
				}
				Integer[] nLastSavedKDA = new Integer[3];
				nLastSavedKDA[0] = nKills;
				nLastSavedKDA[1] = nAssists;
				nLastSavedKDA[2] = nDeaths;
				lKDAPerChamp.add(nLastSavedKDA);
				
				Integer[] nTotalWinsNLosses = new Integer[3];
				nTotalWinsNLosses[0] = nWinsRank;
				nTotalWinsNLosses[1] = nLossesRank;
				nTotalWinsNLosses[2] = nWinsRank + nLossesRank;
				lChampWinLose.add(nTotalWinsNLosses);
				nTotalGames = nWinsRank + nLossesRank;
				double dCalcTotal = dTotalCS / nTotalGames;
				lTotalCS.add(dCalcTotal);
				int nWinRateChamp = (nWinsRank / nTotalGames) * 100;
				lWinRates.add(nWinRateChamp);
				String sLink = arChampKeyPics.get(nIncrement);
				%> <tr class="container individualChamps">
					<td>
					<img src="<%out.println(sLink);
					%>" class="champion" width="64px" height="64px"/>
					<p><%Integer[] nSavedKDA = new Integer[3];
						nSavedKDA = lKDAPerChamp.get(nIncrement);
						double dKDR = nSavedKDA[0] + nSavedKDA[1];
						dKDR = Math.round(dKDR / nSavedKDA[2] * 100) / 100.0;
						out.println(dKDR);%></p>
					<p>
					<% Integer[] nWinLose = new Integer[3];
					   nWinLose = lChampWinLose.get(nIncrement);
					   out.println(nWinLose[2] + "G");
					   out.println(nWinLose[0] + "W");
					   out.println(nWinLose[1] + "L");%>
					</p>
					<p>
					<%
					double dTotalCSDisplay = lTotalCS.get(nIncrement);
					out.println(dTotalCSDisplay);%>
					</p>
					<p>
					<%int nChampWinRate = lWinRates.get(nIncrement);
					out.println(nChampWinRate);%>
					</p>
						</td>
				</tr>
				
			<%
			nIncrement++;
			}
			%>
				</tbody>
			</table>
		</div>
		<%
			ArrayList<String> lPictures = (ArrayList<String>) request.getAttribute("champPics");
			ArrayList<Participant> participants = (ArrayList<Participant>) request.getAttribute("participants");
			ArrayList<ParticipantStats> pt = (ArrayList<ParticipantStats>) request.getAttribute("pt");
			ArrayList<String> arSpells1 = (ArrayList<String>) request.getAttribute("summonerSpells1");
			ArrayList<String> arSpells2 = (ArrayList<String>) request.getAttribute("summonerSpells2");
			ArrayList<String> lMaps = (ArrayList<String>)request.getAttribute("MAPS");
			ArrayList<ItemsList> lItems = (ArrayList<ItemsList>) request.getAttribute("itemsList");
			ArrayList<Long> matchDurations = (ArrayList<Long>) request.getAttribute("matchDurations");
			int nCount = 0;
			int nMapCount = 0;
			for (String sPic : lPictures) {

				ParticipantStats partStats = pt.get(nCount);
				ItemsList itemsList = new ItemsList();
				itemsList = lItems.get(nCount);
				String loseColor = "207, 0, 15, 0.4";
				String winColor = "129, 207, 224, 0.4";
				String sWinorLose = "";
				int nTotalCS = 0;
				Long lgGameDuration = matchDurations.get(nCount);
				if (partStats.isWin()) {
		%>
		<div class="container matches"
			style="background-color: rgba(<%out.println(winColor);%>);">
			<%
				sWinorLose = "VICTORY";
				} else {
			%>
			<div class="container matches"
				style="background-color: rgba(<%out.println(loseColor);%>);">
				<%
					sWinorLose = "DEFEAT";
				}
				%>
				<img width="64px" height="64px" class="champion"
					src="
		<%out.println(sPic);%>">
				<%
					String sSpell1 = arSpells1.get(nCount);
						String sSpell2 = arSpells2.get(nCount);
						double dKDR = partStats.getKills() + partStats.getAssists();
						dKDR = Math.round(dKDR / partStats.getDeaths() * 100) / 100.0;
						nCount++;

				%>
				<img width="32px" height="32px" class="summonerSpell1"
					src="<%out.println(sSpell1);%>" /> <img width="32px" height="32px"
					class="summonerSpell2" src="<%out.println(sSpell2);%>" />
					
				<p class="kda">
					<%
						out.print(partStats.getKills() + " / ");
					%>
					<span style="color: #FF0000"> <%
 	out.print(partStats.getDeaths());
 %>
					</span>
					<%
						out.print(" / " + partStats.getAssists());
					%>
				</p>
					<%
					if(partStats.getDeaths() == 0) {
						String sInfinite = "PERFECT KDA";
						%>			
						<p class="kda2"><%out.println(sInfinite);%></p>
						<%
					} else {
						%>
						<p class="kda1"><%out.println(dKDR + " KDA");%></p>
						<%
					}
					%>
				<div class="itemsSlot">
					<%for(int i = 0; i < 7; i++) {
					%>
						<div id="itemBlock">
						</div>
				<%}
				//https://stackoverflow.com/questions/4177864/checking-if-a-url-exists-or-not
				%>
				</div>
				<%
				if(partStats.getItem0() != 0) {%>
				<img src="<%out.println(itemsList.getsItem1());%>"
				class="items" style="margin-left: 300px"/>
				<% }
				if(partStats.getItem1() != 0) {%>
				<img src="<%out.println(itemsList.getsItem2());%>"
				class="items" style="margin-left:332px"/>
				<% }
				if(partStats.getItem2() != 0) {%>
				<img src="<%out.println(itemsList.getsItem3());%>"
				class="items" style="margin-left:364px"/>
				<% } if(partStats.getItem3() != 0) {%>
				<img src="<%out.println(itemsList.getsItem4());%>"
				class="items" style="margin-left:396px"/>
				<% }
				if(partStats.getItem4() != 0) {%>
				<img src="<%out.println(itemsList.getsItem5());%>"
				class="items" style="margin-left: 428px"/>
				<% }
				if(partStats.getItem5() != 0) {%>
				<img src="<%out.println(itemsList.getsItem6());%>"
				class="items" style="margin-left: 460px"/>
				<% }
				if(partStats.getItem6() != 0) {%>
				<img src="<%out.println(itemsList.getsItem7());%>"
				class="items" style="margin-left: 492px"/>
				<% }%>
				<% if(sWinorLose.equals("VICTORY")) { %>
					<p class="winorlose" style='color:#122e3b;'>
					<% out.println(sWinorLose);
				} else {
					%>
					</p>
					<p class="winorlose" style='color: #82362d;'>
						<% out.println(sWinorLose);
				} 
				String sMap = lMaps.get(nCount - 1);
				%>
			</p>
			<p class="maps">
				<%if(sMap.contains("Draft")) {
					out.println("Normal (Draft Pick)");
				} else if(sMap.contains("Blind")) {
					out.println("Normal (Blind Pick)");
				} else if(sMap.contains("ARAM")) {
					out.println("ARAM");
				}
				else if(sMap.contains("Ranked")) {
					out.println("Ranked Solo/Duo");
				} else {
					out.println(sMap);
				}
				%>
			</p>
			<%if(sWinorLose.equals("DEFEAT")) { %>
			<p class="totalCS" style="margin-top: -5px">
			<% nTotalCS = partStats.getTotalMinionsKilled() + partStats.getNeutralMinionsKilled();
			out.println(nTotalCS); 
			%>
			<img src="images/icons/minion.png"/>
			</p>
			<%
			} else {
				%>
				<p class="totalCS">
				<% nTotalCS = partStats.getTotalMinionsKilled() + partStats.getNeutralMinionsKilled();
				out.println(nTotalCS); %>
				<img src="images/icons/minion.png"/>
				<%} %>
			</p>
			<p class="totalGold">
			<%
			NumberFormat goldCurrency = NumberFormat.getIntegerInstance();
			out.println(goldCurrency.format(partStats.getGoldEarned()));
			%>
			<img src="images/icons/gold.png" style="margin-top: 10px;"/>
			</p>
			<p class="gameDuration">
				<%DecimalFormat df = new DecimalFormat("00.##"); 
				int nSeconds = lgGameDuration.intValue() % 60;
				int nMinutes = lgGameDuration.intValue() / 60;
				out.println(nMinutes + ":" + df.format(nSeconds));%>
			</p>
			</div>
			<%}%>
			</div>
		</div>
</body>
</html>