import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class Jury
 */
@WebServlet("/Risk")
public class Risk extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	State state; 
	String[] names;

	
    public Risk() {
    	//Initialization of Servlet
        super();
    	state = new State(3);
    	names = new String[]{"PLAYER1", "PLAYER2", "PLAYER3"};
    }
    
    void reset(String names){
    	String[] new_names = names.split("-");
    	state = new State(new_names.length);
    	this.names = new_names;
    }
    
    void display(HttpServletRequest request, HttpServletResponse response, int player, String action) throws ServletException, IOException {
    	/*
    	 * There are 3 zones to display on the screen:
    	 * 1.	Top-left: The battle 
    	 * 		+	1.1 World map -> printMap (visible to every player)
    	 * 		+	1.2 How regiments are distributed (visible to every player but customized by private player) -> printPlayerMap
    	 * 
    	 * 2.	Bottom-left:
    	 * 		+	2.1 Cards received by players (private) -> printCard
    	 * 		+	2.2 Winner information (only when game finished) (visible to every player) -> checkFinish
    	 * 
    	 * 3. 	Right:
    	 * 		+ 	3.1 Notifications (customized for player) -> printNotification
    	 * 		+ 	3.1.1 Allow refresh (for every player) and undo (for private player) -> printUpdateZone
    	 * 		+	3.2	Next turn in information (visible for every player)  -> printNextTuneInInformation
    	 * 		+	3.3	Dices (visible for every player) -> printDices
    	 * 		+	3.4	Mission (private) -> printMission
    	 * 	
    	 * 4.	Private data (to communicate with javascript)
    	 */
    	response.setContentType("text/html");
    	response.setCharacterEncoding("utf-8");
    	
		PrintWriter out = response.getWriter();
		if (!action.equals("VIEW")){
			printRedirectionHeader(out, player);
			printMap(out);
			printFooter(out);
			return;
		}
		
		printHeader(out);
		
		printMap(out);
		
		printPlayerMap(out, request, player);
		
		printPrivateData(out, request);
		
		printCards(out, player);
		
		printMission(out, player);
		
		printDices(out, request);
		
		printNotification(out, player);

		printNextTuneInInformation(out);
		
		printUpdateZone(out, player);
		
		if (state.finished()){
			checkFinish(out);
		}
		
		printFooter(out);
    }
    
    void printRedirectionHeader(PrintWriter out, int player){
    	//If action is not VIEW, redirect to action VIEW to avoid unexpected repeated action with automatic refresh
    	String title = "PLAY RISK ONLINE";
		
		out.println("<!DOCTYPE HTML>");
		out.println("<html>");
		out.println("<head><title>" + title + "</title><meta charset=\"UTF-8\" />");
		out.println("<meta http-equiv=\"refresh\" content=\"0; url=Risk?action=VIEW&player=" + player + "\" />");
		out.println("</head>");
		out.println("<body>");
    }
    
    void printDumpHeader(PrintWriter out){
    	//If action is RESET, return a blank page
    	String title = "PLAY RISK ONLINE";
		
		out.println("<!DOCTYPE HTML>");
		out.println("<html>");
		out.println("<head><title>" + title + "</title><meta charset=\"UTF-8\" />");
		out.println("<script type=\"text/javascript\" src=\"show_map.js\"></script>");
		out.println("</head>");
		out.println("<body>");
    }
    
    void printHeader(PrintWriter out){
    	//If action is VIEW, print normal header
    	String title = "PLAY RISK ONLINE";
		
		out.println("<!DOCTYPE HTML>");
		out.println("<html>");
		out.println("<head><title>" + title + "</title><meta charset=\"UTF-8\" />");
		out.println("<script type=\"text/javascript\" src=\"show_map.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"live.js\"></script>");
		out.println("</head>");
		out.println("<body>");
    }
    
    void printMap(PrintWriter out){
    	//The right green background
		out.println("<img src='Img/W.png' style='left:825px; top:10px; width:350px; height:740px; opacity:0.1; position:absolute'>");
		
		//The bottom-left violet background
		out.println("<img src='Img/U.png' style='left:10px; top:460px; width:800px; height:290px; opacity:0.1; position:absolute'>");
		
		//The world map background
    	out.println("<img id='territoryA' src='Img/A.png' style='left:10px; top:10px; width:800px; height:440px; opacity:1; position:absolute'>");
		
    	//Then each territory, highlight when being ATTACKED or being CHOSEN (to move regiments from or to attack from)
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			if (state.attacking_territory_from == i || state.attacking_territory_to == i)
				out.println("<img class='territory' id='territory" + i + "' src='Img/" + i + ".png' style='left:10px; top:10px; width:800px; height:440px; opacity:0.5; position:absolute'>");
			else
				out.println("<img class='territory' id='territory" + i + "' src='Img/" + i + ".png' style='left:10px; top:10px; width:800px; height:440px; opacity:1.0; position:absolute'>");
		}
    }
    
    void printPlayerMap(PrintWriter out, HttpServletRequest request, int player){
    	int active_player = state.currentActivePlayer();
		
		//Use canvas for interaction
		out.println("<canvas id='myCanvas' width='800' height='440' style='left:10px; top:10px; position:absolute; z-index:99; opacity: 0.8'></canvas>");
		
		//Repeat the map background to allow clicking
		out.println("<img src='Img/A.png' style='left:10px; top:10px; width:800px; height:440px; opacity:0; position:absolute; z-index:100;' usemap='#worldmap'>");
    	
		out.println("<map id='worldmap' name='worldmap'>");
		
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			/*
			 * When a territory is clickable?
			 * 	1.	When another territory was chosen:
			 * 		1.1 If it was chosen for attacking from, then all its neighbors belonging to other players will be clickable for the attacker
			 * 		1.2	If it was chosen to move regiments from, then all its connected territories belonging to the attacker will be clickable for the attacker
			 * 	2.	While attacking
			 * 		The attacked territory will be clickable for the defender
			 * 	3.	In first distribution
			 * 		The territories belonging to a player will be clickable for that player
			 *  4.	If a player is able to attack somewhere or to move regiments from
			 *  	The territories belonging to a player will be clickable for that player
			 */
			
			if (state.save && !state.attacking){
				if (state.attacking_territory_from >= 0 && state.neighbors.get(state.attacking_territory_from).contains(i) && state.territories_by_player[i] != player && state.battle_status[player] == state.STATUS_ACTIVE_FOR_ATTACK)
					out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + player + ", " + i + ")' onmouseout='darken(" + i +")';>");		
				if (state.battle_status[player] == state.STATUS_ACTIVE_FOR_FORTIFICATION && state.attacking_territory_from >= 0 && state.areConnected(state.attacking_territory_from, i))
					out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + player + ", " + i + ")' onmouseout='darken(" + i +")';>");					
			}
			
			else if (state.attacking && state.attacking_territory_to == i && player == state.territories_by_player[i]){
				out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + player + ", " + i +")' onmouseout='darken(" + i +")';>");
			} 
			else if (state.territories_by_player[i] == player && state.battle_status[player] == state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){ 
				out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + player + ", " + i +")' onmouseout='darken(" + i +")';>");
			}
			else if (!state.attacking && player == active_player && state.territories_by_player[i] == player){
				//if ( state.battle_status[player] == state.STATUS_ACTIVE_FOR_DISTRIBUTION || state.nb_soldiers_on_territory[i] >= 2)
					out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + player + ", " + i +")' onmouseout='darken(" + i +")';>");
			}		
		}
		
		out.println("</map>");
    }
    
    void printPrivateData(PrintWriter out, HttpServletRequest request){
    	// Data to communicate with javascript
    	out.println("<input type='text' id='territories_by_player' value='" + Arrays.toString(state.territories_by_player) + "'style='display:none;'></input>");
		out.println("<input type='text' id='nb_soldiers_on_territory' value='" + Arrays.toString(state.nb_soldiers_on_territory) + "'style='display:none;'></input>");
		if (state.attacking_territory_from >= 0){
			out.println("<input type='text' id='chosen_territory' value='" + state.attacking_territory_from + "' style='display:none;'></input>");
		}
		else{
			out.println("<input type='text' id='chosen_territory' value='' style='display:none;'></input>");
		}
    }
    
    void checkFinish(PrintWriter out){
    	// When finish, print the player whose mission has been accomplished and his mission card
    	out.println("<form style='color:green; left:10px; top:480px; width:800px; position:absolute'>");
    	for (int player=0; player < state.nb_players; ++player){
        	if (state.missionCompleted(player)){
        		out.println("<h1>" + names[player] + " has accomplished his mission!</h1>");
        		out.println("<img class='card' id='mission' src='Img/M" + state.mission_of_player[player] + ".png' style='left:640px; top:35px; width:140px; height:220px; position:absolute'>");
        		return;
        	}
    	}
    	
    	//Otherwise print the players with the most territories
    	out.print("<h1>Player ");
    	for (Integer player: state.winner()){
    		out.print(player + " ");
    	}
    	out.println(" win(s) with the most territories.</h1>");
    	out.println("</form>");
    }
    
    void printCards(PrintWriter out, int player){
    	if (state.finished())
    		return;
    	
    	// Print cards received by a player in order, with ability to turn back/front the card and possibility to turn in cards
    	out.println("<h3 style='color:green; left:10px; top:460px; position:absolute'>Your cards</h3>");
    	
    	int active_player = state.currentActivePlayer(); 
    	int index = 0;
    	int[] X = {20, 180, 340, 500, 660};
    	int Y = 510;
    	for (Integer card: state.cards_held_by_player.get(player)){
    		out.println("<img class='card' id='card" + card + "' src='Img/CA.png' style='left:" + X[index++] + "px; top:" + Y +"px; width:140px; height:220px; position:absolute' onclick='updowncard(" + (player==active_player?1:0) + ", " + player + ", " + card + ");'>");
    	}
    	
    	if (state.battle_status[player] == state.STATUS_ACTIVE_FOR_TUNE_IN){
    		out.println("<input type='text' id='tune_in_card_1' value='' style='display:none;'></input>");
    		out.println("<input type='text' id='tune_in_card_2' value='' style='display:none;'></input>");
    		out.println("<input type='text' id='tune_in_card_3' value='' style='display:none;'></input>");
    		if (player == active_player && state.ableToTurnIn(player))
    			out.println("<a id='submit_for_tune_in' style='left:10px; top:720px; position:absolute' href=''>Turn in</a>");
    	}
    	
    }
    
    void printMission(PrintWriter out, int player){
    	// Print mission and the stop card
    	// In the first distribution, replace the stop card by the whole deck
    	
    	int[] X = {850, 1010};
    	int Y = 510;
    	
    	out.println("<h3 style='color:green; left:850px; top:460px; position:absolute'>Your mission</h3>");
    	if (IntStream.of(state.battle_status).sum() >= state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION)
    		out.println("<h3 style='color:green; left:1010px; top:460px; position:absolute'>Position of the stop card</h3>");
    	else
    		out.println("<h3 style='color:green; left:1010px; top:460px; position:absolute'>Stop card</h3>");
    	
    	int mission = state.mission_of_player[player];
    	out.println("<img class='card' id='mission' src='Img/MA.png' style='left:" + X[0] + "px; top:" + Y +"px; width:140px; height:220px; position:absolute' onclick='updownmission(" + mission + ");'>");
    	
    	if (IntStream.of(state.battle_status).sum() >= state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){
    		for (int i = state.NB_TERRITORIES - 1; i >= 0; --i){
    			if (i == state.last_card_order || i == (state.last_card_order - 1)){ 				
    				out.println("<img id='last_card' src='Img/CA.png' border='1' style='left:" + (X[1] + i*0.5) + "px; top:" + Y +"px; width:138px; height:216px; position:absolute; border-color:red;'>");
    			} else
    				out.println("<img id='last_card' src='Img/CA.png' style='left:" + (X[1] + i*0.5) + "px; top:" + Y +"px; width:140px; height:220px; position:absolute;'>");
    		}
    	} else    	
    		out.println("<img class='card' id='last_card' src='Img/C" + state.last_card + ".png' style='left:" + X[1] + "px; top:" + Y +"px; width:140px; height:220px; position:absolute'>");
    }
    
    void printDices(PrintWriter out, HttpServletRequest request){
    	// Print dices
    	
    	int[] X = {850, 920, 990};
    	int[] Y = {330, 400};
    	
    	for (int dice = 0; dice < state.attack_dices.length; dice++){
    		if (state.attacking)
    			out.println("<img class='adice' id='A" + dice + "' src='Img/A" + state.attack_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[0] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    		else
    			out.println("<img id='A" + dice + "' src='Img/A" + state.attack_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[0] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    	}
    	
    	for (int dice = 0; dice < state.defend_dices.length; dice++){
    		if (state.defend_dices[dice] > 0)
    			out.println("<img class='ddice' id='D" + dice + "' src='Img/D" + state.defend_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[1] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    		else
    			out.println("<img id='D" + dice + "' src='Img/D" + state.defend_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[1] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    	}
    	
    	out.println("<input type='number' id='attacking' value='" + (state.attacking?1:0) + "' style='display:none;'></input>");
    	out.println("<input type='text' id='defend' value='" + (state.attacking?state.territory_index.get(state.attacking_territory_to):"") + "' style='display:none;'></input>");
    }
    
    void printNotification(PrintWriter out, int player){
    	// Notification
    	
    	int active_player = state.currentActivePlayer(); 
		out.println("<form style='left:850px; top:10px; position:absolute'>");
		
		// Handle TURN IN cáe
		if (player == active_player && state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_TUNE_IN && !state.ableToTurnIn(active_player)){
			out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
		}
		else
			out.println("<input type='text' id='battle_status' value='" + state.battle_status[active_player] + "' style='display:none;'>");
		
		if (state.battle_status[player] > 0)
			out.println("<h3>Now it's your turn.</h3>");
		else if (state.battle_status[active_player] < 10)
			out.println("<h3>Now it's " + names[active_player] + "'s turn.</h3>");
		else
			out.println("<h5>Wait for the others to finish distribution.</h5>");
		
		// FIRST DISTRIBUTION
    	if (IntStream.of(state.battle_status).sum() >= state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){
    		out.println("<h3>Initial distribution.</h3>");
    		out.println("<input type='text' id='battle_status' value='10' style='display:none;'>");
    		out.println("<p>Number of regiments left: <strong>" + state.available_soldiers_to_add[player] + "</strong></p>");
    		out.println("<p>Distribute:<input id='number' type='number' size='10' value=0 onchange='notify()'></p>");
    	}
    	
    	
    	// TURN IN
    	else if (player == active_player && state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_TUNE_IN){
    		if (!state.ableToTurnIn(active_player)){
    			state.finishTuneIn(active_player);
    			out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
        		out.println("<p>Number of regiments left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
        		out.println("<p>Distribute:<input id='number' type='number' size='10' value=0 onchange='notify(" + state.battle_status[active_player] +")'></p>");
    		} else {
        		out.println("<input type='text' id='battle_status' value='1' style='display:none;'></input>");
        		if (state.cards_held_by_player.get(player).size() < 5)
        			out.println("<h3>You can turn in</h3>");
        		else
        			out.println("<h3>Time to turn in</h3>");
    			out.println("<p>Click on 3 cards to turn in</p>");
    			if (state.cards_held_by_player.get(player).size() < 5){
        			out.println("<p>Or skip.</p>");
        			out.println("<a href='Risk?action=FINISH_TUNE_IN&player=" + active_player + "'>SKIP</a>");
    			}
    		}
    	}
    	
    	// DISTRIBUTION
    	else if (player == active_player && state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_DISTRIBUTION){
    		out.println("<h3>You have bonus regiments.</h3>");
    		out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
    		out.println("<p>Number of regiments left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
    		out.println("<p>Distribute:<input id='number' type='number' size='10' value=0 onchange='notify()'></p>");
    	}
    	
    	// ATTACK
    	else if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_ATTACK){
    		out.println("<h3>Attack</h3>");
    		if (state.save){
    			out.println("<h5 style='color:red;'>From: " + state.TERRITORIES[state.attacking_territory_from] + "</h5>");
    		}
    		
    		if (state.attacking){
    			out.println("<h5 style='color:red;'>To: " + state.TERRITORIES[state.attacking_territory_to] + "</h5>");
    		}
    		
    		if (state.save && !state.attacking && player == active_player){
    			int localmax = Math.min(3, state.nb_soldiers_on_territory[state.attacking_territory_from] - 1);
    			if (state.nb_soldiers_on_territory[state.attacking_territory_from] > 1){
    				out.println("<input id='number_range' type='range'  min='1' max='" + localmax + "' value='" + localmax + "' onchange='changerange();' />");
    				out.println("<p id='number_sign' style='color:red; display:inline; '>" + localmax + "</p>");
    			}
    		}
    		
    		out.println("<input type='text' id='battle_status' value='3' style='display:none;'></input>");
    		if (active_player == player && !state.save)
    			out.println("<a href='Risk?action=FINISH_ATTACK&player=" + active_player + "'>Finish attack</a>");
    	}
    	
    	// FORTIFICATION
    	else if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_FORTIFICATION){
    		out.println("<h3>Fortification</h3>");
    		
    		if (player == active_player){
    			out.println("<input type='text' id='battle_status' value='4' style='display:none;'></input>");
    		
    			if (state.save){
    				out.println("<h5 style='color:red;'>From: " + state.TERRITORIES[state.attacking_territory_from] + "</h5>");
    				out.println("<p>Number to move: <input id='number' type='number' size='10' value=0 onchange='notify()'></p>");
    			}

    			if (!state.save)
    				out.println("<a href='Risk?action=FINISH_FORTIFY&player=" + active_player + "'>Finish fortification</a>");
    		}
    	}
    	
		out.println("<p id='notification'></p>");
		out.println("</form>");
    }
    
    void printNextTuneInInformation(PrintWriter out){
    	// Next turn in information
		out.println("<form style='left:850px; top:210px; position:absolute'>");
		out.println("<h3>Next turn in: <strong>" + state.NB_TUNED_IN_SOLDIERS[state.next_tune_in_index] + "</strong> regiments.</h3>");
		out.println("</form>");
    }
    
    void printUpdateZone(PrintWriter out, int player){
    	// Refresh and undo
    	out.println("<form style='left:850px; top:280px; position:absolute'>");
    	out.println("<a href='Risk?action=VIEW&player=" + player + "'>Refresh</a>");
    	if (state.save && !state.attacking && player == state.currentActivePlayer()){
    		out.println("<a href='Risk?action=UNDO'>Undo</a>");
    	}
    	
    	out.println("</form>");
    }
    
    void printFooter(PrintWriter out){

		out.println("</body>");
		out.println("</html>");
    }
    
    void refuse(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	response.setContentType("text/html");
    	response.setCharacterEncoding("utf-8");
    	
		PrintWriter out = response.getWriter();
		printDumpHeader(out);
		out.println("<h1>Sorry, you are not authorized to play this game.</h1>");
		printFooter(out);
    }
    
    void test(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html");
    	response.setCharacterEncoding("utf-8");
    	
		PrintWriter out = response.getWriter();
		printDumpHeader(out);
		
		printp(out, "Number of players:" + state.nb_players);
		printp(out, "Territories by players:" + Arrays.toString(state.territories_by_player));
		printp(out, "Nb_soldiers_on_territory: " + Arrays.toString(state.nb_soldiers_on_territory));
		printp(out, "Current card: " + state.current_card);
		printp(out, "Battle status: " + Arrays.toString(state.battle_status));
		printp(out, "Available regiments to add: " + Arrays.toString(state.available_soldiers_to_add));
		printp(out, "Attacking: " + state.attacking);
		printp(out, "Attack dices: " + Arrays.toString(state.attack_dices));
		printp(out, "Defend dices: " + Arrays.toString(state.defend_dices));
		printp(out, "Save: " + state.save);
		printp(out, "Attacking from: " + state.attacking_territory_from);
		printp(out, "Attacking to: " + state.attacking_territory_to);
		printp(out, "Shuffle: " + Arrays.toString(state.shuffled_cards.toArray()));
		printp(out, "EP: " + state.last_card_order);
		printp(out, "P: " + state.last_card);
		
		printFooter(out);
    	
    }
    
    void printp(PrintWriter out, String s){
    	out.println("<p>" + s + "</p>");
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		
		if (action.equals("TEST")){
			test(request, response);
			return;
		}
		
		if (action.equals("RESET")){
			String names = request.getParameter("names");
			reset(names);
		}
		
		int player = -1;
		
		if (request.getCookies() != null){
			String name = request.getCookies()[0].getValue();
			for (int i = 0; i < this.names.length; ++i){
				if (this.names[i].equals(name)){
					player = i;
				}
			}
		}
		
		if (player == -1){
			refuse(request, response);
			return;
		}

		if (action.equals("VIEW") || (state.battle_status[player] == 0 && (!(state.attacking && state.territories_by_player[state.attacking_territory_to] == player)) || (state.battle_status[player] == 3 && state.attacking) ) ){
			display(request, response, player, action);
			return;
		}
		
		if (action.equals("ADD_ONE")){
			int territory = state.territory_index.get(request.getParameter("territory"));
			state.addOne(player, territory);
			display(request, response, player, action);
		}
		
		if (action.equals("TUNE_IN")){
			int card1 = Integer.parseInt(request.getParameter("card1"));
			int card2 = Integer.parseInt(request.getParameter("card2"));
			int card3 = Integer.parseInt(request.getParameter("card3"));
			
			state.tuneIn(player, new int[]{card1, card2, card3});
			display(request, response, player, action);
		}
		
		if (action.equals("FINISH_TUNE_IN")){
			state.finishTuneIn(player);
			display(request, response, player, action);
		}
		
		if (action.equals("ADD_MANY")){
			int territory = state.territory_index.get(request.getParameter("territory"));
			int number = Integer.parseInt(request.getParameter("number"));
			state.addMany(player, territory, number);
			display(request, response, player, action);
		}
		
		if (action.equals("ATTACK")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			state.attacking_territory_from = territory_from;
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			state.attacking_territory_to = territory_to;
			int number = Integer.parseInt(request.getParameter("number"));
			state.attack(player, territory_from, territory_to, number);
			display(request, response, player, action);
		}
		
		if (action.equals("DEFEND")){
			int territory_from = state.attacking_territory_from;
			int territory_to = state.attacking_territory_to;		
			state.defend(player, territory_from, territory_to);
			display(request, response, player, action);
		}
		
		if (action.equals("FINISH_ATTACK")){
			state.finishAttack(player);
			state.resetDices();
			display(request, response, player, action);
		}
		
		if (action.equals("FORTIFY")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			int number = Integer.parseInt(request.getParameter("number"));
			state.fortify(player, territory_from, territory_to, number);
			display(request, response, player, action);
		}
		
		if (action.equals("FINISH_FORTIFY")){
			state.finishFortification(player);
			display(request, response, player, action);
		}
		
		if (action.equals("SAVE")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			state.attacking_territory_from = territory_from;
			state.save();
			state.resetDices();
			display(request, response, player, action);
		}
		
		if (action.equals("UNDO")){
			state.attacking_territory_from = -1;
			state.unsave();
			display(request, response, player, action);
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
