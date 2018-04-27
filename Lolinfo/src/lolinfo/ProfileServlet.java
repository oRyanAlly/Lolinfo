package lolinfo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.net.*;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.derby.iapi.util.StringUtil;
import org.apache.tomcat.util.codec.binary.StringUtils;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.api.endpoints.league.dto.LeaguePosition;
import net.rithms.riot.api.endpoints.match.dto.MatchReference;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.match.dto.ParticipantStats;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.MatchList;
import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.ChampionList;
import net.rithms.riot.api.endpoints.static_data.dto.SummonerSpell;
import net.rithms.riot.api.endpoints.static_data.dto.SummonerSpellList;
import net.rithms.riot.constant.Platform;

/**
 * Servlet implementation class ProfileServlet
 */
@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProfileServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ApiConfig config = new ApiConfig().setKey("RGAPI-110d29cf-05be-40ee-b346-d0bd3dab99ea");
		RiotApi api = new RiotApi(config);

		String playerName = request.getParameter("findPlayer");
		String serverName = request.getParameter("serverName");
		Platform pServer = Platform.getPlatformByName(serverName);

		Connection connect = null;
		Statement statement = null;

		try {
			Class.forName("org.apache.derby.jdbc.ClientDriver");
			connect = DriverManager.getConnection("jdbc:derby://localhost:1527/LolinfoDB;create=true", "rally",
					"password123");
			/**
			 * Insertion of Champion and SummonerSpells Data
			 */
			String sQuery = "SELECT * FROM APP.CHAMPIONS";

			Statement stmt = connect.createStatement();
			ResultSet rs = stmt.executeQuery(sQuery);
			if (rs.next() == false) {
				System.out.println("MADE IT");
				// Looping through Champions table to see if there is no data
				ChampionList championList = api.getDataChampionList(pServer, null, "8.8.1", false,
						ChampionListTags.ALL);
				Map<String, Champion> championsByName = championList.getData();
				for (Champion champion : championsByName.values()) {
					String sInsertQuery = "INSERT INTO APP.CHAMPIONS(ID, TITLE, NAME, CHAMPKEY)  "
							+ " VALUES (?, ?, ?, ?)";
					PreparedStatement pstmt = connect.prepareStatement(sInsertQuery);
					pstmt.setInt(1, champion.getId());
					pstmt.setString(2, champion.getTitle());
					pstmt.setString(3, champion.getName());
					pstmt.setString(4, champion.getKey());
					pstmt.execute();
				}
			}
			String sSummonerSpells = "SELECT * FROM APP.SUMMONERSPELLS ORDER BY ID";
			Statement stmtSpells = connect.createStatement();
			ResultSet rsSpells = stmtSpells.executeQuery(sSummonerSpells);
			if (rsSpells.next() == false) {
				// Looping through Summoner Spells
				SummonerSpellList lSummonerSpells = api.getDataSummonerSpellList(pServer);
				Map<String, SummonerSpell> summonerSpells = lSummonerSpells.getData();
				for (SummonerSpell ss : summonerSpells.values()) {
					String sInsertSpells = "INSERT INTO APP.SUMMONERSPELLS(ID, NAME, SPELLKEY)  " + " VALUES (?, ?, ?)";
					PreparedStatement pstmtSpells = connect.prepareStatement(sInsertSpells);
					pstmtSpells.setInt(1, ss.getId());
					pstmtSpells.setString(2, ss.getName());
					pstmtSpells.setString(3, ss.getKey());
					pstmtSpells.execute();
				}
			}
			// Find Summoner
			Summoner summoner = api.getSummonerByName(pServer, playerName);
			long summonerID = summoner.getId();
			Set<LeaguePosition> leaguePositions = api.getLeaguePositionsBySummonerId(pServer, summonerID);
			int summonerIconID = summoner.getProfileIconId();
			long accountId = summoner.getAccountId();
			String sProfilePicture = "http://ddragon.leagueoflegends.com/cdn/8.8.1/img/profileicon/" + summonerIconID
					+ ".png";

			// Display Champion's Images and Save Games
			MatchList matchList = api.getRecentMatchListByAccountId(pServer, accountId);
			List<String> lPictures = new ArrayList<String>();
			List<Match> matches = new ArrayList<Match>();
			List<Long> gameIds = new ArrayList<Long>();
			for (MatchReference matchReference : matchList.getMatches()) {
				int nChampID = matchReference.getChampion();
				String sFindChamp = "SELECT CHAMPKEY FROM APP.CHAMPIONS WHERE ID= " + nChampID;
				Statement stmtFind = connect.createStatement();
				ResultSet rsChampName = stmtFind.executeQuery(sFindChamp);
				while (rsChampName.next()) {
					String sChampName = rsChampName.getString("CHAMPKEY");
					sChampName = sChampName.replaceAll("\\s+", "");
					String sChampPic = "https://ddragon.leagueoflegends.com/cdn/8.8.1/img/champion/" + sChampName
							+ ".png";
					lPictures.add(sChampPic);
					gameIds.add(matchReference.getGameId());
					request.setAttribute("champPics", lPictures);
				}
			}
			List<Participant> participants = new ArrayList<Participant>();
			List<ParticipantStats> pt = new ArrayList<ParticipantStats>();
			List<String> lSummonerSpells1 = new ArrayList<String>();
			List<String> lSummonerSpells2 = new ArrayList<String>();
			List<Integer> lQueueIDs = new ArrayList<Integer>();
			List<ItemsList> lItems = new ArrayList<ItemsList>();
			ArrayList<Long> matchDurations = new ArrayList<Long>();
			// Display recent (20) matches
			for (Long gameId : gameIds) {
				Match match = api.getMatch(pServer, gameId);
				Participant participant = match.getParticipantBySummonerId(summonerID);
				ParticipantStats participantStat = participant.getStats();
				String sFindSummonerSpell1 = "SELECT SPELLKEY FROM APP.SUMMONERSPELLS WHERE ID= "
						+ participant.getSpell1Id();
				String sFindSummonerSpell2 = "SELECT SPELLKEY FROM APP.SUMMONERSPELLS WHERE ID= "
						+ participant.getSpell2Id();
				Statement stmtFindSpell1 = connect.createStatement();
				Statement stmtFindSpell2 = connect.createStatement();

				ResultSet rsSpell1 = stmtFindSpell1.executeQuery(sFindSummonerSpell1);
				ResultSet rsSpell2 = stmtFindSpell2.executeQuery(sFindSummonerSpell2);
				while (rsSpell1.next()) {
					String sSpell1 = rsSpell1.getString("SPELLKEY");
					String sSpellLink1 = "http://ddragon.leagueoflegends.com/cdn/8.8.1/img/spell/" + sSpell1 + ".png";
					lSummonerSpells1.add(sSpellLink1);
				}
				while (rsSpell2.next()) {
					String sSpell2 = rsSpell2.getString("SPELLKEY");
					String sSpellLink2 = "http://ddragon.leagueoflegends.com/cdn/8.8.1/img/spell/" + sSpell2 + ".png";
					lSummonerSpells2.add(sSpellLink2);
				}
				int nQueueID = match.getQueueId();
				lQueueIDs.add(nQueueID);
				pt.add(participantStat);
				ItemsList itemsList = new ItemsList();
				itemsList.setsItem1(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem0() + ".png");
				itemsList.setnItem1(participantStat.getItem0());
				itemsList.setsItem2(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem1() + ".png");
				itemsList.setnItem2(participantStat.getItem1());

				itemsList.setsItem3(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem2() + ".png");
				itemsList.setnItem3(participantStat.getItem2());

				itemsList.setsItem4(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem3() + ".png");
				itemsList.setnItem4(participantStat.getItem3());

				itemsList.setsItem5(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem4() + ".png");
				itemsList.setnItem5(participantStat.getItem4());

				itemsList.setsItem6(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem5() + ".png");
				itemsList.setnItem6(participantStat.getItem5());

				itemsList.setsItem7(
						"http://ddragon.leagueoflegends.com/cdn/8.7.1/img/item/" + participantStat.getItem6() + ".png");
				itemsList.setnItem7(participantStat.getItem6());

				lItems.add(itemsList);
				matchDurations.add(match.getGameDuration());
				request.setAttribute("matchDurations", matchDurations);
				request.setAttribute("participants", participants);
				request.setAttribute("pt", pt);
				request.setAttribute("summonerSpells1", lSummonerSpells1);
				request.setAttribute("summonerSpells2", lSummonerSpells2);
			}
			for (ItemsList item : lItems) {
				if (checkImageURL(item.getsItem1()) == false) {
					item.setsItem1(
							"http://ddragon.leagueoflegends.com/cdn/6.24.1/img/item/" + item.getnItem1() + ".png");

				} else if (checkImageURL(item.getsItem2()) == false) {
					item.setsItem2(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem2() + ".png");
				} else if (checkImageURL(item.getsItem3()) == false) {
					item.setsItem3(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem3() + ".png");

				} else if (checkImageURL(item.getsItem4()) == false) {
					item.setsItem4(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem4() + ".png");

				} else if (checkImageURL(item.getsItem5()) == false) {
					item.setsItem5(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem5() + ".png");

				} else if (checkImageURL(item.getsItem6()) == false) {
					item.setsItem6(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem6() + ".png");

				} else if (checkImageURL(item.getsItem7()) == false) {
					item.setsItem7(
							"http://ddragon.leagueoflegends.com/cdn/8.8.1/img/item/" + item.getnItem7() + ".png");

				}
				request.setAttribute("itemsList", lItems);
			}
			List<String> lMaps = new ArrayList<String>();
			for (Integer queueIDs : lQueueIDs) {
				String sFindMap = "SELECT DESCRIPTION FROM APP.MATCHMAKINGQUEUE WHERE ID= " + queueIDs;
				Statement stmtFindMap = connect.createStatement();
				ResultSet rsFindMap = stmtFindMap.executeQuery(sFindMap);
				while (rsFindMap.next()) {
					lMaps.add(rsFindMap.getString("DESCRIPTION"));
				}
			}
			request.setAttribute("MAPS", lMaps);
			// Pick random background
			Random r = new Random();
			int nBGPicker = r.nextInt(1) + 1;
			String sWallpaper;
			if (nBGPicker == 1) {
				sWallpaper = "images/wallpapers/bgJax.jpg";
			} else {
				sWallpaper = "images/wallpapers/bgZiggs.jpg";
			}
			request.setAttribute("setBackground", sWallpaper);
			System.out.println("Name:" + summoner.getName());
			System.out.println("Summoner ID: " + summoner.getId());
			System.out.println("Account ID: " + summoner.getAccountId());
			System.out.println("Summoner Level: " + summoner.getSummonerLevel());
			System.out.println("Profile Icon ID: " + summoner.getProfileIconId());

			// Grab Rank Details
			for (LeaguePosition leaguePosition : leaguePositions) {
				int nWins = leaguePosition.getWins();
				int nLoses = leaguePosition.getLosses();
				String sTier = leaguePosition.getTier();
				String sPicTier = "images/" + sTier + ".png";
				String sLeagueName = leaguePosition.getLeagueName();
				int nLeaguePoints = leaguePosition.getLeaguePoints();
				String sRank = leaguePosition.getRank();
				System.out.println("Wins:" + leaguePosition.getWins());
				System.out.println("Loses:" + leaguePosition.getLosses());
				System.out.println("Tier:" + leaguePosition.getTier());

				request.setAttribute("nWins", nWins);
				request.setAttribute("nLoses", nLoses);
				request.setAttribute("sPicTier", sPicTier);
				request.setAttribute("sTier", sTier);
				request.setAttribute("sLeagueName", sLeagueName);
				request.setAttribute("nLeaguePoints", nLeaguePoints);
				request.setAttribute("sRank", sRank);
			}
			request.setAttribute("summonerName", summoner.getName());
			request.setAttribute("summonerPicture", sProfilePicture);

			// Grab Seasonal Champion Statistics
			// 420 - Ranked Games
			Set<Integer> lsQueueID = new HashSet<>();
			lsQueueID.add(420);

			Set<Integer> lsChampIDs = new HashSet<>();

			Set<Integer> lsSeasons = new HashSet<>();
			int nSeasonID = 11;
			lsSeasons.add(nSeasonID);
			MatchList mSeasonStats = api.getMatchListByAccountId(pServer, accountId, lsChampIDs, lsQueueID, lsSeasons);
			List<Integer> lChampionsS8 = new ArrayList<>();
			ArrayList<Long> mSeasonGameIDS = new ArrayList<Long>();
			if (mSeasonStats.getMatches().size() != 0) {
				ArrayList<Participant> arParticipants = new ArrayList<Participant>();
				for (MatchReference matchReference : mSeasonStats.getMatches()) {
					mSeasonGameIDS.add(matchReference.getGameId());
				}				
				for (Long seasonGameID : mSeasonGameIDS) {
					Match match = api.getMatch(pServer, seasonGameID);
					Participant participant = match.getParticipantByAccountId(accountId);
					int nNewChamp = 0;
					nNewChamp = participant.getChampionId();
					lChampionsS8.add(nNewChamp);
					arParticipants.add(participant);
				}
				request.setAttribute("S8Stats", arParticipants);
			}
			List<Integer> updatedChamps = new ArrayList<>();
			List<Integer[]> revisedChampList = new ArrayList<>();
			int nCount = 0;
			for(Integer nChamp : lChampionsS8) {
				if(updatedChamps.contains(nChamp) != true) {
					int nCurrChamp = nChamp;
					updatedChamps.add(nCurrChamp);
				}
			}
			for(Integer i : updatedChamps) {
				Integer[] currChamp = new Integer[2];
				currChamp[0] = i;
				currChamp[1] = Collections.frequency(lChampionsS8, i);
				revisedChampList.add(currChamp);
			} 
			Collections.sort(revisedChampList, new sortList());

			List<String> arChampKeyIMGS = new ArrayList<String>();
			for(Integer[] individualChamps : revisedChampList) {
				String sFindChamp = "SELECT CHAMPKEY FROM APP.CHAMPIONS WHERE ID= " + individualChamps[0];
				Statement stmtFindIndi = connect.createStatement();
				ResultSet rsIndi = stmtFindIndi.executeQuery(sFindChamp);
				while(rsIndi.next()) {
					String sIndivKey = rsIndi.getString("CHAMPKEY");
					sIndivKey = sIndivKey.replaceAll("\\s+", "");
					String sChampKeyPic = "https://ddragon.leagueoflegends.com/cdn/8.8.1/img/champion/" + sIndivKey
							+ ".png";
					arChampKeyIMGS.add(sChampKeyPic);
				}
			}
			request.setAttribute("champKeyPics", arChampKeyIMGS);
			

			request.setAttribute("rankChamps", revisedChampList);
			
			request.getRequestDispatcher("/Profile.jsp").forward(request, response);

		} catch (RiotApiException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// https://stackoverflow.com/questions/4177864/checking-if-a-url-exists-or-not
	public static boolean checkImageURL(String URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	//https://www.geeksforgeeks.org/collections-sort-java-examples/
	public class sortList implements Comparator<Integer[]> {
		public int compare (Integer[] nA, Integer[] nB) {
			return nB[1] - nA[1];
		}
	}
}