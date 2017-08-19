import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

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
    	save = false;
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
		
		out.println("<img id='territoryA' src='Img/A.png' style='margin-left:10px; margin-top:10px; width:800px; height:440px; opacity:1; position:fixed'>");
		
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			out.println("<img id='territory" + i + "' src='Img/" + i + ".png' style='margin-left:10px; margin-top:10px; width:800px; height:440px; opacity:0.4; position:fixed'>");
		}
		
		out.println("<canvas id='myCanvas' width='800' height='440' style=' position:absolute; z-index:99; opacity: 0.8'></canvas>");
		
		out.println("<img src='Img/A.png' style='margin-left:10px; margin-top:10px; width:800px; height:440px; opacity:0; position:absolute; z-index:100;' usemap='#worldmap'>");
		out.println("<map id='worldmap' name='worldmap'>");
		
		for (int i = 0; i < state.NB_TERRITORIES; ++i){
			String href = "";
			if (state.okForAddOne(active_player, i))
				href = "href='Risk?action=ADD_ONE&player=" + active_player + "&territory=" + state.TERRITORIES[i] + "'"; 
				out.println("<area id='area" + i + "' shape='rect' onmouseover='brighten(" + active_player + ", " + i +")' onmouseout='darken(" + i +")'" + href + " ;>");
		}
		
		out.println("</map>");
		
		out.println("<input type='text' id='territories_by_player' value='" + Arrays.toString(state.territories_by_player) + "'style='display:none;'></input>");
		out.println("<input type='text' id='nb_soldiers_on_territory' value='" + Arrays.toString(state.nb_soldiers_on_territory) + "'style='display:none;'></input>");
		if (save){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			out.println("<input type='text' id='chosen_territory' value='" + territory_from + "' style='display:none;'></input>");
			save = false;
		}
		else{
			out.println("<input type='text' id='chosen_territory' value='' style='display:none;'></input>");
		}
		//out.println("<input type='text' id='chosen_territory_to' value='' style='display:none;'></input>");
		
		out.println("<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>");
		out.println("<form style='margin-left:10px; top:480px; position:absolute'>");
		out.println("<h3>Now it's turn of player " + active_player + "</h3>");
		defineForm(out);
		out.println("</form>");
		
		/*
		String[] Description = state.toString().split("\n");
		for (String paragraph: Description){
			out.println("<p>" + paragraph + "</p>");
		}
		*/
		out.println("</body>");
		out.println("</html>");
    }
    
    void defineForm(PrintWriter out){
    	int active_player = state.currentActivePlayer();    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){
    		out.println("<p>Number of soldiers left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
    		out.println("<p>Click on a territory to add additional soldiers</p>");
    		out.println("<input type='text' id='battle_status' value='10' style='display:none;'></input>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_TUNE_IN){
    		if (!state.ableToTurnIn(active_player)){
    			state.finishTuneIn(active_player);
    			out.println("<input type='text' id='battle_status' value='2' style='display:none;'>");
        		out.println("<p>Number of soldiers left: <strong>" + state.available_soldiers_to_add[active_player] + "</strong></p>");
        		out.println("<p>Click on a territory to add additional soldiers</p>");
        		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0></p>");
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
    		out.println("<p>Click on a territory to add additional soldiers</p>");
    		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0></p>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_ATTACK){
    		out.println("<input type='text' id='battle_status' value='3' style='display:none;'></input>");
    		out.println("<p id='from_notification'>Click to choose a territory to attack from.</p>");
    		out.println("<p id='to_notification'>Then a territory to attack to.</p>");
    		out.println("<a href='Risk?action=FINISH_ATTACK&player=" + active_player + "'>Finish attack</a>");
    		return;
    	}
    	
    	if (state.battle_status[active_player] == state.STATUS_ACTIVE_FOR_FORTIFICATION){
    		out.println("<input type='text' id='battle_status' value='4' style='display:none;'></input>");
    		out.println("<p id='from_notification'>Click to choose a territory to move soldiers from.</p>");
    		out.println("<p>Please choose the number of soldiers to distribute: <input id='number' type='number' value=0></p>");
    		out.println("<p id='from_notification'>Then click to choose a territory to move soldiers to.</p>");
    		out.println("<p id='to_notification'>Then a territory to attack to.</p>");
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
			int card0 = Integer.parseInt(request.getParameter("card0"));
			int card1 = Integer.parseInt(request.getParameter("card1"));
			int card2 = Integer.parseInt(request.getParameter("card2"));
			
			state.tuneIn(player, new int[]{card0, card1, card2});
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
		
		if (action.equals("KEEP_ON_ATTACK")){
			int territory_from = state.territory_index.get(request.getParameter("territory_from"));
			int territory_to = state.territory_index.get(request.getParameter("territory_to"));
			state.keepOnAttacking(player, territory_from, territory_to);
			display(request, response, player);
		}
		
		if (action.equals("FINISH_ATTACK")){
			state.finishAttack(player);
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
			save = true;
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
