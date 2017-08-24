/*
 * Author 	: 	Nhut DOAN NGUYEN
 * Date		: 	18/08/2017
 * Purpose	:	For fun
 */

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.Arrays;

public class State {
	
	/*
	 * A state is composed by the following fields:
	 * +	Number of players (by default, 3)
	 * +	Which territories are occupied by which players
	 * +	Mission of a player: an array
	 * +	Cards held by a player: an array
	 * 
	 * Constant
	 * +	Symbol on cards: an array
	 * -	Missions: a dictionary
	 * -	Name of territories: a dictionary
	 * -	Number of tuned-in soldiers: a list
	 */
	
	final int NB_TERRITORIES = 42;
	final int NB_MISSIONS = 12;
	final int[] NB_TUNED_IN_SOLDIERS = {4, 6, 8, 10, 12, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
	
	final int STATUS_INACTIVE = 0;
	final int STATUS_ACTIVE_FOR_TUNE_IN = 1;
	final int STATUS_ACTIVE_FOR_DISTRIBUTION = 2;
	final int STATUS_ACTIVE_FOR_ATTACK = 3;
	final int STATUS_ACTIVE_FOR_FORTIFICATION = 4;
	final int STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION = 10;
	
	//To be modified
	final int[] SYMBOL_ON_CARD = {	
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2, 
									0, 1, 2, 0, 1, 2 
								};
	
	final String[] TERRITORIES = new String[]{ 
			"ALASKA", "NORTH-WEST TERRITORY", "ONTARIO", "EASTERN CANADA", "GREENLAND", "ALBERTA",
			"WESTERN UNITED STATES", "EASTERN UNITED STATES", "CENTRAL AMERICA",  "VENEZUELA", "BRAZIL", "PERU",
			"ARGENTINA", "ICELAND", "GREAT BRITAIN", "SCADINAVIA", "NORTHERN EUROPE", "WESTERN EUROPE",
			"SOUTHERN EUROPE", "UKRAINE", "NORTH_AFRICA", "EGYPT", "EASTERN_AFRICA", "CONGO",
			"SOUTHERN AFRICA", "MADAGASCAR", "MIDDLE EAST", "AFGHANISTAN", "URAL", "SIBERIA", 
			"CHINA", "INDIA", "YAKUTSK", "IRKUTSK", "MONGOLIA", "KAMCHATKA", 
			"JAPAN", "SOUTHEAST ASIA", "INDONESIA", "NEW GUINEA", "WESTERN AUSTRALIA", "EASTERN AUSTRALIA"	
		};
	
	
	final int[][] NEIGHBORS = {
			{1, 5, 35}, 				{0, 2, 4, 5}, 				{1, 3, 4, 5, 6, 7}, 		{2, 4, 7}, 				{1, 2, 3, 13}, 				{0, 1, 2, 6},
			{5, 2, 7, 8}, 				{3, 2, 6, 8}, 				{6, 7, 9}, 					{8, 10, 11}, 			{9, 11, 12, 20}, 			{9, 10, 12},
			{10, 11},	 				{4, 14, 15}, 				{13, 15, 16, 17}, 			{13, 14, 16, 19}, 		{14, 15, 17, 18, 19}, 		{14, 16, 18, 20},
			{16, 17, 19, 20, 21, 26}, 	{15, 16, 18, 26, 27, 28}, 	{10, 17, 18, 21, 22, 23}, 	{18, 20, 22, 26}, 		{20, 21, 23, 24, 25, 26}, 	{20, 22, 24},
			{22, 23, 25}, 				{22, 24}, 					{18, 19, 21, 22, 27, 31}, 	{19, 26, 28, 30, 31}, 	{19, 27, 29, 30}, 			{28, 30, 32, 33, 34},
			{27, 28, 29, 31, 34, 37}, 	{26, 27, 30, 37}, 			{29, 33, 35}, 				{29, 32, 34, 35}, 		{29, 30, 33, 35, 36}, 		{0, 32, 33, 34, 36},
			{34, 35}, 					{30, 31, 38}, 				{37, 39, 40}, 				{38, 40, 41}, 			{38, 39, 41}, 				{39, 40}
		};
	
	final int BEGIN_NORTH_AMERICA = 0;
	final int END_NORTH_AMERICA = 8;
	final int NORTH_AMERICA_VALUE = 5;
	
	final int BEGIN_SOUTH_AMERICA = 9;
	final int END_SOUTH_AMERICA = 12;
	final int SOUTH_AMERICA_VALUE = 2;
	
	final int BEGIN_EUROPE = 13;
	final int END_EUROPE = 19;
	final int EUROPE_VALUE = 5;
	
	final int BEGIN_AFRICA = 20;
	final int END_AFRICA = 25;
	final int AFRICA_VALUE = 3;
	
	final int BEGIN_ASIA = 26;
	final int END_ASIA = 37;
	final int ASIA_VALUE = 7;
	
	final int BEGIN_AUSTRALIA = 38;
	final int END_AUSTRALIA = 41;
	final int AUSTRALIA_VALUE = 2;
	
	final String[] MISSIONS = new String[]{ 
			"CONTROL NORTH AMERICA AND AUSTRALIA",
			"CONTROL EUROPE AND AUSTRALIA",
			"CONTROL NORTH AMERICA AND AFRICA",
			"CONTROL ASIA AND SOUTH AMERICA",
			"CONTROL ASIA AND AFRICA",
			"CONTROL 18 TERRITORIES WITH 2 SOLDIERS ON EACH",
			"CONTROL 24 TERRITORIES",
			"KILL ALL RED",
			"KILL ALL BLUE",
			"KILL ALL YELLOW",
			"KILL ALL GREEN",
			"KILL ALL BLACK"
		};

	final int HUMAN = 0;
	final int HORSE = 1;
	final int CANON = 2;
	
	HashMap<String, Integer> territory_index;
	ArrayList<ArrayList<Integer>> neighbors;
	
	int nb_players;
	int[] territories_by_player;
	int[] nb_soldiers_on_territory;
	int[] mission_of_player;
	ArrayList<ArrayList<Integer>> cards_held_by_player;
	ArrayList<Integer> shuffled_cards;
	int current_card;
	int[] battle_status;
	int[] available_soldiers_to_add;
	boolean attacking;
	int[] attack_dices;
	int[] defend_dices;
	
	int next_tune_in_index;
	boolean save;
	int attacking_territory_from;
	int attacking_territory_to;
	
	public State(int nb_players){
		//Read territories name
		this.territory_index = new HashMap<String, Integer>();
		for (int i = 0; i < NB_TERRITORIES; i++){
			this.territory_index.put(TERRITORIES[i], i);
		}
		
		//Read territories'neighbors
		this.neighbors = new ArrayList<ArrayList<Integer>>();
		
		for (int i = 0; i < NB_TERRITORIES; i++){
			ArrayList<Integer> local_array_list = new ArrayList<Integer>();
			for (int j = 0; j < NEIGHBORS[i].length; j++){
				local_array_list.add(NEIGHBORS[i][j]);
			}
			this.neighbors.add(i, local_array_list);
		}
		
		//Initialize nb_players
		this.nb_players = (nb_players > 5) ? 5 : nb_players;
		
		//Randomly distribute the territories
		this.territories_by_player = new int[NB_TERRITORIES];
		
		ArrayList<Integer> local_range = new ArrayList<Integer>();
		for (int i = 0; i < NB_TERRITORIES; i++){
			local_range.add(i);
		}
		
		Collections.shuffle(local_range);
		int local_i = 0;
		for (Integer i: local_range){
			this.territories_by_player[i] = local_i++ % this.nb_players;
		}
			
		//By default, on each territory there is one soldier
		this.nb_soldiers_on_territory = new int[NB_TERRITORIES];
		for (int i = 0; i < NB_TERRITORIES; i++){
			this.nb_soldiers_on_territory[i] = 1;
		}
		
		//Shuffle missions
		this.mission_of_player = new int[nb_players];
		ArrayList<Integer> local_missions = new ArrayList<Integer>();
		for (int i = 0; i < NB_MISSIONS - (5 - this.nb_players); i++){
			local_missions.add(i);
		}
		
		Collections.shuffle(local_missions);
		
		for (int player=0; player < this.nb_players; player++){
			this.mission_of_player[player] = local_missions.get(player);
			if (this.mission_of_player[player] == 7 + player){
				this.mission_of_player[player] = local_missions.get(5 + player);
			}
		}
		
		//Set turn_in_index
		this.next_tune_in_index = 0;
		
		//Initialize original_battle_status
		this.battle_status = new int[this.nb_players];
		for (int player=0; player < this.nb_players; ++player)
			this.battle_status[player] = STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION;
		
		//Soldiers to add
		this.available_soldiers_to_add = new int[this.nb_players];
		for (int player = 0; player < this.nb_players; player++){
			this.available_soldiers_to_add[player] = 21;
			//this.available_soldiers_to_add[player] = 1;
		}
		
		//Cards held by players
		this.cards_held_by_player = new ArrayList<ArrayList<Integer>>();
		Collections.shuffle(local_range);
		this.shuffled_cards = local_range;
		for (int player = 0; player < this.nb_players; player++){
			this.cards_held_by_player.add(player, new ArrayList<Integer>());
		}
		
		//Current card
		this.current_card = 0;
		
		//Attack dice
		this.attack_dices = new int[]{0, 0, 0};
		this.defend_dices = new int[]{0, 0};
		
		//Unsave
		this.save = false;
		
    	attacking_territory_from = -1;
    	attacking_territory_to = -1;
	}
	
	public ArrayList<Integer> getTerritoriesOccupedBy(int player){
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < NB_TERRITORIES; i++){
			if (this.territories_by_player[i] == player)
				res.add(i);
		}
		return res;
	}
	
	public String toString(){
		String description = "";
		
		for (int player = 0; player < this.nb_players; player++){
			description +=  "Player " + player + " is controling territories: ";
			ArrayList<Integer> territories_occupied_by_player = getTerritoriesOccupedBy(player);
			for (Integer territory: territories_occupied_by_player){
				description += TERRITORIES[territory] + ", ";
			}
			description += "with ";
			for (Integer territory: territories_occupied_by_player){
				description += this.nb_soldiers_on_territory[territory] + " ";
			}
			description += "soldiers, respectively.\n";
		}
		
		int current_active_player = currentActivePlayer();
		description += "Active player: " + current_active_player + ".\n"; 
		if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION)
			description += "Please add a soldier to one of your territory.";
		else if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_TUNE_IN)
			description += "Please tune in.";
		else if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_DISTRIBUTION)
			description += "You have " + this.available_soldiers_to_add[current_active_player] + " additional soldiers. Please distribute them.";		
		else if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_ATTACK)
			description += "Please attack.";
		else if (this.battle_status[current_active_player] == this.STATUS_ACTIVE_FOR_FORTIFICATION)
			description += "Please fortify.";
		
		return description;
	}
	
	public int getNextPlayer(int player){
		return (player + 1) % this.nb_players;
	}
	
	public int currentActivePlayer(){
		int current_active_player = -1;
		for (int player = 0; player < this.nb_players; player++){
			if (this.battle_status[player] > 0){
				current_active_player = player;
				break;
			}
		}
		
		return current_active_player;
	}
	
	public void goToNextStep(){
		int current_active_player = currentActivePlayer();
		
		//Check if games finished
		if (finished()){
			System.out.println("-------------------GAMES FINISHED--------------");
			return;
		}
		
		//If not fortified, remain the same player
		if (this.battle_status[current_active_player] < STATUS_ACTIVE_FOR_FORTIFICATION){
			this.battle_status[current_active_player] += 1;
			System.out.println("Player " + current_active_player + " goes to step " + this.battle_status[current_active_player]);
		}
		
		//If fortified, next player
		else if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_FORTIFICATION){
			this.battle_status[getNextPlayer(current_active_player)] = 1;
			this.battle_status[current_active_player] = 0;
			System.out.println("Player " + getNextPlayer(current_active_player) + " goes to step " + this.battle_status[getNextPlayer(current_active_player)]);
		}
		
		//First distribution case
		else if (this.battle_status[current_active_player] == STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION && current_active_player == this.nb_players - 1 && this.available_soldiers_to_add[0] == 0){
			this.battle_status[getNextPlayer(current_active_player)] = 1;
			this.battle_status[current_active_player] = 0;
			System.out.println("Player " + getNextPlayer(current_active_player) + " goes to step " + this.battle_status[getNextPlayer(current_active_player)]);
		}
		
		else{
			this.battle_status[getNextPlayer(current_active_player)] = 10;
			this.battle_status[current_active_player] = 0;
			System.out.println("Player " + getNextPlayer(current_active_player) + " goes to step " + this.battle_status[getNextPlayer(current_active_player)]);
		}
		
	}
	
	//THIS IS FOR FIRST DISTRIBUTION
	public boolean okForAddOne(int player, int territory){
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION){
			return false;
		}
		
		if (this.territories_by_player[territory] != player){
			return false;
		}
		
		if (this.available_soldiers_to_add[player] < 1){
			return false;
		}
		
		return true;
	}
	
	public void addOne(int player, int territory){
		if (!okForAddOne(player, territory))
			return;
		
		this.nb_soldiers_on_territory[territory] += 1;
		this.available_soldiers_to_add[player] -= 1;
		
		goToNextStep();
	}
	
	//THIS IS FOR TUNE IN
	public boolean hasThreeOfSameKind(ArrayList<Integer> symbols, int player, int kind){
		if (Collections.frequency(symbols, kind) >= 3){
			return true;
		}
		return false;
	}
	
	public boolean hasThreeOfDifferentKinds(ArrayList<Integer> symbols, int player){
		if (symbols.contains(HUMAN) && symbols.contains(HORSE) && symbols.contains(CANON)){
			return true;
		}	
		return false;
	}
	
	public boolean ableToTurnIn(int player){
		
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_TUNE_IN){
			return false;
		}
		
		ArrayList<Integer> symbols = new ArrayList<Integer>();
		for (Integer card: this.cards_held_by_player.get(player)){
			symbols.add(SYMBOL_ON_CARD[card]);
		}
		
		return hasThreeOfSameKind(symbols, player, HUMAN) || hasThreeOfSameKind(symbols, player, HORSE) || hasThreeOfSameKind(symbols, player, CANON) || hasThreeOfDifferentKinds(symbols, player);
	}
	
	public boolean obligedToTuneIn(int player){		
		return this.cards_held_by_player.get(player).size() >= 5;
	}
	
	public void tuneIn(int player, int[] cards){
		System.out.println("Player " + player + " wants to tune in.");
		
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_TUNE_IN){
			System.out.println("Error: Status inactive");
			return;
		}
		
		if (!ableToTurnIn(player)){
			System.out.println("Warning: Not able to tune in.");
			finishTuneIn(player);
			return;
		}
		
		int a = SYMBOL_ON_CARD[cards[0]];
		int b = SYMBOL_ON_CARD[cards[1]];
		int c = SYMBOL_ON_CARD[cards[2]];
		
		if ((a==b && b==c) || a*a + b*b + c*c == 5){
			//Only one of the following will have effects
			for (int i = this.cards_held_by_player.get(player).size()-1; i >= 0 ; --i){
				if (this.cards_held_by_player.get(player).get(i) == cards[0] || this.cards_held_by_player.get(player).get(i) == cards[1] || this.cards_held_by_player.get(player).get(i) == cards[2])
					this.cards_held_by_player.get(player).remove(i);
			}
			this.available_soldiers_to_add[player] += NB_TUNED_IN_SOLDIERS[this.next_tune_in_index ++];
		}
		
		System.out.println("Player " + player + " has " + this.cards_held_by_player.get(player).size() + " cards now.");
		finishTuneIn(player);
		
	}
	
	public boolean okForFinishTuneIn(int player){
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_TUNE_IN){
			return false;
		}
		
		return true;
	}
	
	public void finishTuneIn(int player){
		if (!okForFinishTuneIn(player)){
			return;
		}
		
		if (!obligedToTuneIn(player)){
			this.available_soldiers_to_add[player] += pointsFromTerritories(player) + pointsFromContinents(player);
			goToNextStep();
			System.out.println("Player " + player + " can have " + this.available_soldiers_to_add[player] + " more soldiers.");
		} else {
			System.out.println("Error: Player " + player + " has to tune in first.");
		}
	}
	
	public int pointsFromTerritories(int player){
		System.out.println("Player " + player + " has " + this.getTerritoriesOccupedBy(player).size() / 3 + " points from territories.");
		return Math.max(this.getTerritoriesOccupedBy(player).size() / 3, 3);
	}
	
	public boolean isNorthAmericaControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_NORTH_AMERICA; i <= END_NORTH_AMERICA; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}		
		return occupied;
	}
	
	public boolean isSouthAmericaControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_SOUTH_AMERICA; i <= END_SOUTH_AMERICA; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}
		return occupied;
	}
	
	public boolean isEuropeControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_EUROPE; i <= END_EUROPE; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}
		return occupied;
	}
	
	public boolean isAfricaControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_AFRICA; i <= END_AFRICA; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}
		return occupied;
	}
	
	public boolean isAsiaControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_ASIA; i <= END_ASIA; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}
		return occupied;
	}
	
	public boolean isAustraliaControlledBy(int player){
		boolean occupied = true;
		for (int i = BEGIN_AUSTRALIA; i <= END_AUSTRALIA; i++){
			if (this.territories_by_player[i] != player){
				occupied = false;
				break;
			}
		}
		return occupied;
	}	
	
	public int pointsFromContinents(int player){
		int res = 0;
		
		
		if (isNorthAmericaControlledBy(player))
			res += NORTH_AMERICA_VALUE;
		
		
		if (isSouthAmericaControlledBy(player))
			res += SOUTH_AMERICA_VALUE;
		

		if (isEuropeControlledBy(player))
			res += EUROPE_VALUE;
		
		
		if (isAfricaControlledBy(player))
			res += AFRICA_VALUE;
		
		
		if (isAsiaControlledBy(player))
			res += ASIA_VALUE;
		

		if (isAustraliaControlledBy(player))
			res += AUSTRALIA_VALUE;
		
		System.out.println("Player " + player + " has " + res + " points from continents.");
		return res;
	}
	
	//THIS IS FOR DISTRIBUTION
	public boolean okForAddMany(int player, int territory, int number){
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_DISTRIBUTION && IntStream.of(this.battle_status).sum() < 10){
			return false;
		}
		
		if (this.territories_by_player[territory] != player){
			return false;
		}
		
		if (this.available_soldiers_to_add[player] < number){
			return false;
		}
		
		if (this.battle_status[player] == STATUS_ACTIVE_FOR_DISTRIBUTION && number <= 0){
			return false;
		}
		
		if (IntStream.of(this.battle_status).sum() >= 10 && this.nb_soldiers_on_territory[territory] + number < 1){
			return false;
		}
		
		return true;
	}
	
	public void addMany(int player, int territory, int number){
		if (this.okForAddMany(player, territory, number)){
			this.nb_soldiers_on_territory[territory] += number;
			this.available_soldiers_to_add[player] -= number;
		}		
		
		if (this.available_soldiers_to_add[player] == 0){
			if (IntStream.of(this.battle_status).sum() >= 10){
				this.battle_status[player] = 0;
				if (IntStream.of(this.battle_status).sum() == 0){
					this.battle_status[0] = 1;
				}
			}
			
			else {
				if (IntStream.of(this.battle_status).sum() < 10)
					goToNextStep();
			}
		}
			
	}
	
	//THIS IS FOR ATTACKS
	public void attack(int player, int territory_from, int territory_to){
		/* Constraints:
		 * 	-	Status allows.
		 * 	-	territory_from belongs to player
		 * 	-	territory_to does not belong to player
		 *  -	territory_to is a neighbor of territory_to
		 *  -	nb_of_soldiers on territory_from is at least 2
		 *  
		 * Process:
		 * 	-	Generate a random array for dices roll of player
		 *  -	Generate another one for dices of the concurrent
		 *  -	Compare them
		 *  -	If the territory is occupied, move soldiers from territory_from to territory_to
		 */
		
		
		System.out.println("Player " + player + " is attacking from " + TERRITORIES[territory_from] + " to " + TERRITORIES[territory_to] + ".");
		
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_ATTACK){
			System.out.println("Error: Status inactive");
			unsave();
			attacking_territory_from = -1;
			attacking_territory_to = -1;
			return;
		}
		
		if (this.attacking){
			System.out.println("Error: Attacking");
			return;
		}
		
		if (this.territories_by_player[territory_from] != player){
			System.out.println("Error: Territory_from is not his territory.");
			attacking_territory_from = -1;
			attacking_territory_to = -1;
			unsave();
			return;
		}		
		
		if (!this.neighbors.get(territory_from).contains(territory_to)){
			System.out.println("Error: These 2 territories are not neighbors.");
			attacking_territory_from = -1;
			attacking_territory_to = -1;
			unsave();
			return;
		}
		
		if (this.territories_by_player[territory_to] == player){
			System.out.println("Error: Territory_to is already his territory.");
			attacking_territory_from = -1;
			attacking_territory_to = -1;
			unsave();
			return;
		}

		if (this.nb_soldiers_on_territory[territory_from] < 2){
			System.out.println("Error: Insuffisant force");
			attacking_territory_from = -1;
			attacking_territory_to = -1;
			unsave();
			return;
		}

		
		this.attacking = true;
		this.attack_dices = generateRandomNumber(Math.min(3, this.nb_soldiers_on_territory[territory_from] - 1));
	
	}
	
	public void defend(int player, int territory_from, int territory_to){
		if (!this.attacking){
			System.out.println("Error: Status inactive");
			return;
		}
		
		if (this.territories_by_player[territory_to] != player){
			System.out.println("Error: Status inactive");
			return;
		}
		
		this.defend_dices = generateRandomNumber(Math.min(2, this.nb_soldiers_on_territory[territory_to]));
		
		int[] res_of_attack = compare(attack_dices, defend_dices);
		
		this.nb_soldiers_on_territory[territory_from] += res_of_attack[0];
		this.nb_soldiers_on_territory[territory_to] += res_of_attack[1];
		
		if (this.nb_soldiers_on_territory[territory_to] == 0){
			System.out.println(TERRITORIES[territory_to] + " lost, no under control of player " + player);
			this.territories_by_player[territory_to] = this.territories_by_player[territory_from];
			this.nb_soldiers_on_territory[territory_to] += Math.min(3, this.nb_soldiers_on_territory[territory_from] - 1);
			this.nb_soldiers_on_territory[territory_from] -= Math.min(3, this.nb_soldiers_on_territory[territory_from] - 1);
		}
		
		this.attacking = false;
		this.unsave();
		this.attacking_territory_from = -1;
		this.attacking_territory_to = -1;
	}
	
	public void resetDices(){
		this.attack_dices = new int[]{0, 0, 0};
		this.defend_dices = new int[]{0, 0};
	}
	
	public int[] generateRandomNumber(int number){
		int[] res = new int[number];
		Random Random = new Random();
		for (int i = 0; i < number; i++){
			res[i] = -1 - Random.nextInt(6);
		}
		Arrays.sort(res);
		
		for (int i = 0; i < number; i++){
			res[i] = -res[i];
		}
		return res;
	} 
	
	public int[] compare(int[] a, int[] b){
		int[] res = new int[]{0, 0};
		for (int i = 0; i < Math.min(a.length, b.length); i++){
			if (a[i] > b[i]){
				res[1] -= 1;
			}
			else {
				res[0] -= 1;
			}
		}
		return res;
	}
	
	public void finishAttack(int player){
		
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_ATTACK){
			System.out.println("Error: Status inactive");
			return;
		}
		
		goToNextStep();
	}
	
	//FORTIFICATION
	public void fortify(int player, int territory_from, int territory_to, int nb){
		/* Constraints:
		 * 	-	Status allows.
		 * 	-	territory_from belongs to player
		 * 	-	territory_to belongs to player
		 *  -	they are connected
		 *  -	each territory has at least 1 soldier
		 *  
		 */
		System.out.println("Player " + player + " is moving " + nb + " soldiers from " + TERRITORIES[territory_from] + " to " + TERRITORIES[territory_to] + ".");
		
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_FORTIFICATION){
			System.out.println("Error: Status inactive");
			return;
		}
		
		if (this.territories_by_player[territory_from] != player){
			System.out.println("Error: Territory_from is not his territory.");
			return;
		}		
		
		if (this.territories_by_player[territory_to] != player){
			System.out.println("Error: Territory_to is already his territory.");
			return;
		}

		if (nb > this.nb_soldiers_on_territory[territory_from] - 1){
			System.out.println("Error: Insuffisant force for " + TERRITORIES[territory_from]);
			return;
		}
		
		if (!areConnected(territory_from, territory_to)){
			System.out.println("Error: These territories are not connected.");
			return;
		}
		
		this.nb_soldiers_on_territory[territory_from] -= nb;
		this.nb_soldiers_on_territory[territory_to] += nb;
		
		this.attacking_territory_from = -1;
		this.attacking_territory_to = -1;
		
		this.unsave();
		
		//System.out.println("Action completed.");
	}
	
	public void finishFortification(int player){
		if (this.battle_status[player] != STATUS_ACTIVE_FOR_FORTIFICATION){
			System.out.println("Error: Status inactive");
			return;
		}
		
		takeACard(player);
		
		goToNextStep();
	}
	
	public void takeACard(int player){
		Integer random_card = this.shuffled_cards.remove(0);
		this.cards_held_by_player.get(player).add(random_card);
		System.out.println("Player " + player + " has received card " + random_card);
	}
	
	public ArrayList<Integer> getConnectedComponent(int territory){
		ArrayList<Integer> res = new ArrayList<Integer>();
		return explore(res, territory, this.territories_by_player[territory]);
	}
	
	public ArrayList<Integer> explore(ArrayList<Integer> res, int territory, int player){
		//DFS
		res.add(territory);
		for (Integer another_territory: this.neighbors.get(territory)){
			if (this.territories_by_player[another_territory] == player && !res.contains(another_territory)){
				res = explore(res, another_territory, player);
			}
		}		
		return res;
	}
	
	public boolean areConnected(int territory_from, int territory_to){
		return getConnectedComponent(territory_from).contains(territory_to);
	}
	
	//CHECK IF FINISHED
	
	public boolean missionCompleted(int player){
		if (this.mission_of_player[player] == 0 && this.isNorthAmericaControlledBy(player) && this.isAustraliaControlledBy(player))
			return true;
		if (this.mission_of_player[player] == 1 && this.isEuropeControlledBy(player) && this.isAustraliaControlledBy(player))
			return true;
		if (this.mission_of_player[player] == 2 && this.isNorthAmericaControlledBy(player) && this.isAfricaControlledBy(player))
			return true;
		if (this.mission_of_player[player] == 3 && this.isAsiaControlledBy(player) && this.isSouthAmericaControlledBy(player))
			return true;
		if (this.mission_of_player[player] == 4 && this.isAsiaControlledBy(player) && this.isAfricaControlledBy(player))
			return true;
		
		if (this.mission_of_player[player] == 5 && this.getTerritoriesOccupedBy(player).size() >= 18){
			int count = 0;
			for (Integer i: this.getTerritoriesOccupedBy(player)){
				if (this.nb_soldiers_on_territory[i] >= 2){
					count += 1;
				}
			}
			if (count >= 18)
				return true;
		}
		
		if (this.mission_of_player[player] == 6 && this.getTerritoriesOccupedBy(player).size() >= 24)
			return true;
		
		for (int concurrent = 0; concurrent < this.nb_players; concurrent++)
			if ((this.mission_of_player[player] == 7 + concurrent) && this.getTerritoriesOccupedBy(concurrent).size() == 0){
				return true;
			}
		
		return false;
	}
	
	public boolean all_round_finished(){
		return this.next_tune_in_index == 13;
	}
	
	public boolean finished(){
		if (all_round_finished()) 
			return true;
		for (int player = 0; player < this.nb_players; player++){
			if (missionCompleted(player))
				return true;
		}
		
		return false;
	}
	
	public void save(){
		this.save = true;
	}
	
	public void unsave(){
		this.save = false;
	}
	
	public ArrayList<Integer> winner(){
		
		ArrayList<Integer> winners = new ArrayList<Integer>();
		for (int player = 0; player < this.nb_players; player++){
			if (missionCompleted(player)){
				winners.add(player);
				return winners;
			}
		}

		int max = 0;
		for (int player = 0; player < this.nb_players; player++){
			if (this.getTerritoriesOccupedBy(player).size() > max){
				max = this.getTerritoriesOccupedBy(player).size();
			}			
		}
		
		for (int player = 0; player < this.nb_players; player++){
			if (this.getTerritoriesOccupedBy(player).size() == max){
				winners.add(player);
			}			
		}
		
		return winners;
	}
}
