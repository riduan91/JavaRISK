import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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
	boolean save;

    public Risk() {
        super();
    	state = new State(3);
    }
    
    void display(HttpServletRequest request, HttpServletResponse response, int player) throws ServletException, IOException {
    	int active_player = state.currentActivePlayer();
    	
    	response.setContentType("text/html");
    	response.setCharacterEncoding("utf-8");
    	
		PrintWriter out = response.getWriter();
		String title = "PLAY RISK ONLINE";
		
		out.println("<!DOCTYPE HTML>");
		out.println("<html>");
		out.println("<head><title>" + title + "</title><meta charset=\"UTF-8\" /><script type=\"text/javascript\" src=\"show_map.js\"></script><link rel='stylesheet' type='text/css' href='main.css'></head>");
		out.println("<body>");
		
		out.println("<img id='territoryA' src='Img/A.png' style='left:10px; top:10px; width:800px; height:440px; opacity:1; position:absolute'>");
		
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			out.println("<img id='territory" + i + "' src='Img/" + i + ".png' style='left:10px; top:10px; width:800px; height:440px; opacity:1.0; position:absolute'>");
		}
		
		out.println("<canvas id='myCanvas' width='800' height='440' style='left:10px; top:10px; position:absolute; z-index:99; opacity: 0.8'></canvas>");
		
		out.println("<img src='Img/A.png' style='left:10px; top:10px; width:800px; height:440px; opacity:0; position:absolute; z-index:100;' usemap='#worldmap'>");
		out.println("<map id='worldmap' name='worldmap'>");
		
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			String href = "";
			if (state.okForAddOne(active_player, i))
				href = "href='Risk?action=ADD_ONE&player=" + active_player + "&territory=" + state.TERRITORIES[i] + "'"; 
			if (state.attacking){
				out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + state.territories_by_player[state.territory_index.get(request.getParameter("territory_to"))] + ", " + i +")' onmouseout='darken(" + i +")'" + href + " ;>");
			} else{
				out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + active_player + ", " + i +")' onmouseout='darken(" + i +")'" + href + " ;>");
			}		
		}
		
		out.println("</map>");
		
		
		out.println("<input type='text' id='territories_by_player' value='" + Arrays.toString(state.territories_by_player) + "'style='display:none;'></input>");
		out.println("<input type='text' id='nb_soldiers_on_territory' value='" + Arrays.toString(state.nb_soldiers_on_territory) + "'style='display:none;'></input>");
		if (state.save){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			out.println("<input type='text' id='chosen_territory' value='" + territory_from + "' style='display:none;'></input>");
		}
		else{
			out.println("<input type='text' id='chosen_territory' value='' style='display:none;'></input>");
		}
		//out.println("<input type='text' id='chosen_territory_to' value='' style='display:none;'></input>");
		defineCards(out);
		defineMission(out);
		defineDices(out, request);
		out.println("<form style='left:820px; top:10px; position:absolute'>");
		out.println("<h3>Now it's turn of player " + active_player + "</h3>");
		defineForm(out, player);
		out.println("<p id='notification'></p>");
		out.println("</form>");
		
		out.println("<form style='left:820px; top:210px; position:absolute'>");
		out.println("<h3>Next tune in: <strong>" + state.NB_TUNED_IN_SOLDIERS[state.next_tune_in_index] + "</strong> regiments.</h3>");
		out.println("</form>");
		/*
		String[] Description = state.toString().split("\n");
		for (String paragraph: Description){
			out.println("<p>" + paragraph + "</p>");
		}
		*/
		
		if (state.finished()){
			checkFinish(out);
		}
		
		out.println("</body>");
		out.println("</html>");
    }
    
    void checkFinish(PrintWriter out){
    	out.println("<h1 style='left:10px; top:720px; width:140px; height:220px; position:absolute'");
    	for (int player=0; player < state.nb_players; ++player){
        	if (state.missionCompleted(player)){
        		out.println("<p>Player " + player + " has completed his mission.</p>");
        		out.println("<img id='mission' src='Img/M" + state.mission_of_player[player] + ".png' style='width:140px; height:220px; position:absolute'>");
        		return;
        	}
    	}
    	
    	out.print("<p>Player ");
    	for (Integer player: state.winner()){
    		out.print(player + " ");
    	}
    	out.println(" win(s) with the most territories.");
    }
    
    void defineCards(PrintWriter out){
    	int active_player = state.currentActivePlayer(); 
    	int index = 0;
    	int[] X = {10, 175, 340, 505, 670};
    	int[] Y = {480, 480, 480, 480, 480};
    	for (Integer card: state.cards_held_by_player.get(active_player)){
    		out.println("<img id='card" + card + "' src='Img/CA.png' style='left:" + X[index] + "px; top:" + Y[index++] +"px; width:140px; height:220px; position:absolute' onclick='updowncard(" + active_player + ", " + card + ");'>");
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_TUNE_IN){
    		out.println("<input type='text' id='tune_in_card_1' value='' style='display:none;'></input>");
    		out.println("<input type='text' id='tune_in_card_2' value='' style='display:none;'></input>");
    		out.println("<input type='text' id='tune_in_card_3' value='' style='display:none;'></input>");
    		out.println("<a id='submit_for_tune_in' style='left:10px; top:720px; position:absolute' href=''>Tune in</a>");
    	}
    	
    }
    
    void defineMission(PrintWriter out){
    	int active_player = state.currentActivePlayer(); 
    	int X = 850;
    	int Y = 480;
    	int mission = state.mission_of_player[active_player];
    	out.println("<img id='mission' src='Img/MA.png' style='left:" + X + "px; top:" + Y +"px; width:140px; height:220px; position:absolute' onclick='updownmission(" + mission + ");'>");
    }
    
    void defineDices(PrintWriter out, HttpServletRequest request){
    	int[] X = {850, 920, 990};
    	int[] Y = {330, 400};
    	
    	for (int dice = 0; dice < state.attack_dices.length; dice++){
    		out.println("<img id='A" + dice + "' src='Img/A" + state.attack_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[0] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    	}
    	
    	for (int dice = 0; dice < state.defend_dices.length; dice++){
    		out.println("<img id='D" + dice + "' src='Img/D" + state.defend_dices[dice] + ".png' style='left:" + X[dice] + "px; top:" + Y[1] +"px; width:50px; height:50px; position:absolute; opacity:0.7'>");
    	}
    	
    	out.println("<input type='number' id='attacking' value='" + (state.attacking?1:0) + "' style='display:none;'></input>");
    	out.println("<input type='text' id='defend' value='" + (state.attacking?state.territory_index.get(request.getParameter("territory_to")):"") + "' style='display:none;'></input>");
    }
    
    void defineForm(PrintWriter out, int player){
    	int active_player = state.currentActivePlayer();    	
    	if (IntStream.of(state.battle_status).sum() >= state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){
    		out.println("<input type='text' id='battle_status' value='10' style='display:none;'>");
    		out.println("<p>Number of soldiers left: <strong>" + state.available_soldiers_to_add[player] + "</strong></p>");
    		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0 onchange='notify()'></p>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_TUNE_IN){
    		if (!state.ableToTurnIn(active_player)){
    			state.finishTuneIn(active_player);
    			out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
        		out.println("<p>Number of soldiers left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
        		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0 onchange='notify(" + state.battle_status[active_player] +")'></p>");
    			return;
    		} else {
        		out.println("<input type='text' id='battle_status' value='1' style='display:none;'></input>");
    			out.print("You are holding the following cards: ");
    			for (Integer card: state.cards_held_by_player.get(active_player)){
    				out.print(card + ", ");
    			}
    			out.println("");
    			out.println("Please click on your cards to tune in or move to next step.");
    			out.println("<a href='Risk?action=FINISH_TUNE_IN&player=" + active_player + "'>Finish tune in</a>");
    		}
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_DISTRIBUTION){
    		out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
    		out.println("<p>Number of soldiers left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
    		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0 onchange='notify()'></p>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_ATTACK){
    		out.println("<input type='text' id='battle_status' value='3' style='display:none;'></input>");
    		out.println("<a href='Risk?action=FINISH_ATTACK&player=" + active_player + "'>Finish attack</a>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_FORTIFICATION){
    		out.println("<input type='text' id='battle_status' value='4' style='display:none;'></input>");
    		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0 onchange='notify()'></p>");
    		out.println("<a href='Risk?action=FINISH_FORTIFY&player=" + active_player + "'>Finish fortification</a>");
    		return;
    	}
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	int player = Integer.parseInt(request.getParameter("player"));
		String action = request.getParameter("action");
		
		if (action.equals("VIEW")){
			display(request, response, player);
		}
		
		if (action.equals("ADD_ONE")){
			int territory = state.territory_index.get(request.getParameter("territory"));
			state.addOne(player, territory);
			display(request, response, player);
		}
		
		if (action.equals("TUNE_IN")){
			int card1 = Integer.parseInt(request.getParameter("card1"));
			int card2 = Integer.parseInt(request.getParameter("card2"));
			int card3 = Integer.parseInt(request.getParameter("card3"));
			
			state.tuneIn(player, new int[]{card1, card2, card3});
			display(request, response, player);
		}
		
		if (action.equals("FINISH_TUNE_IN")){
			state.finishTuneIn(player);
			display(request, response, player);
		}
		
		if (action.equals("ADD_MANY")){
			int territory = state.territory_index.get(request.getParameter("territory"));
			int number = Integer.parseInt(request.getParameter("number"));
			state.addMany(player, territory, number);
			display(request, response, player);
		}
		
		if (action.equals("ATTACK")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			state.attack(player, territory_from, territory_to);
			display(request, response, player);
		}
		
		if (action.equals("DEFEND")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			state.defend(player, territory_from, territory_to);
			display(request, response, player);
		}
		
		if (action.equals("FINISH_ATTACK")){
			state.finishAttack(player);
			state.resetDices();
			display(request, response, player);
		}
		
		if (action.equals("FORTIFY")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			int number = Integer.parseInt(request.getParameter("number"));
			state.fortify(player, territory_from, territory_to, number);
			display(request, response, player);
		}
		
		if (action.equals("FINISH_FORTIFY")){
			state.finishFortification(player);
			display(request, response, player);
		}
		
		if (action.equals("SAVE")){
			state.save();
			state.resetDices();
			display(request, response, player);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
