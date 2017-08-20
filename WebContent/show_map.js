var TERRITORIES = [ 
			"ALASKA", "NORTH-WEST TERRITORY", "ONTARIO", "EASTERN CANADA", "GREENLAND", "ALBERTA",
			"WESTERN UNITED STATES", "EASTERN UNITED STATES", "CENTRAL AMERICA",  "VENEZUELA", "BRAZIL", "PERU",
			"ARGENTINA", "ICELAND", "GREAT BRITAIN", "SCADINAVIA", "NORTHERN EUROPE", "WESTERN EUROPE",
			"SOUTHERN EUROPE", "UKRAINE", "NORTH_AFRICA", "EGYPT", "EASTERN_AFRICA", "CONGO",
			"SOUTHERN AFRICA", "MADAGASCAR", "MIDDLE EAST", "AFGHANISTAN", "URAL", "SIBERIA", 
			"CHINA", "INDIA", "YAKUTSK", "IRKUTSK", "MONGOLIA", "KAMCHATKA", 
			"JAPAN", "SOUTHEAST ASIA", "INDONESIA", "NEW GUINEA", "WESTERN AUSTRALIA", "EASTERN AUSTRALIA"	
];

var NEIGHBORS = [
	[1, 5, 35], 				[0, 2, 4, 5], 				[1, 3, 4, 5, 6, 7], 		[2, 4, 7], 				[1, 2, 3, 13], 				[0, 1, 2, 6],				
	[5, 2, 7, 8], 				[3, 2, 6, 8], 				[6, 7, 9], 					[8, 10, 11], 			[9, 11, 12, 20], 			[9, 10, 12],				
	[10, 11],	 				[4, 14, 15], 				[13, 15, 16, 17], 			[13, 14, 16, 19], 		[14, 15, 17, 18, 19], 		[14, 16, 18, 20],			
	[16, 17, 19, 20, 21, 26], 	[15, 16, 18, 26, 27, 28], 	[10, 17, 18, 21, 22, 23], 	[18, 20, 22, 26], 		[20, 21, 23, 24, 25, 26], 	[20, 22, 24],				
	[22, 23, 25], 				[22, 24], 					[18, 19, 21, 22, 27, 31], 	[19, 26, 28, 30, 31], 	[19, 27, 29, 30], 			[28, 30, 32, 33, 34],		
	[27, 28, 29, 31, 34, 37], 	[26, 27, 30, 37], 			[29, 33, 35], 				[29, 32, 34, 35], 		[29, 30, 33, 35, 36], 		[0, 32, 33, 34, 36],		
	[34, 35], 					[30, 31, 38], 				[37, 39, 40], 				[38, 40, 41], 			[38, 39, 41], 				[39, 40]					
];

var ORIGINAL_COORDINATES = [
    [27, 80, 88, 135], 		[94, 80, 250, 120],		[194, 152, 251, 200],	[266, 151, 319, 219],	[314, 40, 401, 109], 	[114, 134, 181, 193],
    [119, 209, 200, 251],	[192, 260, 276, 290],	[137, 305, 199, 385],	[204, 391, 299, 406],	[285, 434, 356, 503],	[236, 493, 275, 515], 
    [239, 561, 303, 614],	[422, 125, 473, 151],	[366, 196, 469, 244],	[487, 88, 569, 152],	[494, 219, 557, 255],	[413, 273, 484, 359],
    [491, 283, 570, 312],	[588, 127, 673, 201],	[427, 440, 546, 497],	[539, 403, 595, 430],	[584, 480, 660, 514], 	[528, 532, 597, 550],
    [540, 602, 613, 658],	[653, 598, 707, 670],	[614, 327, 715, 418],	[679, 236, 758, 275],	[710, 133, 756, 198],	[764, 56, 823, 156],
    [795, 285, 912, 337],	[737, 353, 813, 424],	[851, 53, 911, 91],		[841, 141, 884, 182],	[826, 218, 916, 260],	[927, 60, 1022, 119],
    [956, 197, 1010, 293],	[835, 377, 889, 443], 	[824, 506, 921, 553],	[943, 477, 1008, 520],	[873, 621, 980, 656],	[950, 553, 1052, 614]
]

var STATUS_INACTIVE = 0;
var STATUS_ACTIVE_FOR_TUNE_IN = 1;
var STATUS_ACTIVE_FOR_DISTRIBUTION = 2;
var STATUS_ACTIVE_FOR_ATTACK = 3;
var STATUS_ACTIVE_FOR_FORTIFICATION = 4;
var STATUS_ACTIVE_FOR_FIRST_DISTRIBUTION = 10;

var NB_TERRITORIES = 42;

var COLOR = ['red', 'green', 'blue', 'orange', 'black'];

var original_width = 1200;
var original_height = 711;
var margin_left = 10;
var margin_top = 10;
var canvas_width = 800;
var canvas_height = 440;

function brighten(player, i){
	battle_status = document.getElementById("battle_status").value;

	document.getElementById("territory" + i).style.opacity = 0.5;
	for (var j = 0; j < NEIGHBORS[i].length; j++){
		document.getElementById("territory" + NEIGHBORS[i][j]).style.opacity = 0.5;
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_DISTRIBUTION){
		var number = document.getElementById("number").value;
		if (number > 0){
			document.getElementById("area" + i).href = "Risk?action=ADD_MANY&player=" + player + "&territory=" + TERRITORIES[i] + "&number=" + number;
		}
		return;
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_ATTACK){
		if (document.getElementById("chosen_territory").value != ""){
			document.getElementById("area" + i).href = "Risk?action=ATTACK&player=" + player + "&territory_from=" + TERRITORIES[document.getElementById("chosen_territory").value] + "&territory_to=" + TERRITORIES[i];
		} else {
			document.getElementById("area" + i).href = "Risk?action=SAVE&player=" + player + "&territory_from=" + TERRITORIES[i];
		}
		return;
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_FORTIFICATION){
		if (document.getElementById("chosen_territory").value != ""){
			var number = document.getElementById("number").value;
			if (number > 0){
				document.getElementById("area" + i).href = "Risk?action=FORTIFY&player=" + player + "&territory_from=" + TERRITORIES[document.getElementById("chosen_territory").value] + "&territory_to=" + TERRITORIES[i] + "&number=" + number;
			}
		} else {
			document.getElementById("area" + i).href = "Risk?action=SAVE&player=" + player + "&territory_from=" + TERRITORIES[i];
		}
		return;
	}
}

function darken(i){
	document.getElementById("territory" + i).style.opacity = 1;
	
	for (var j = 0; j < NEIGHBORS[i].length; j++){
		document.getElementById("territory" + NEIGHBORS[i][j]).style.opacity = 1;
	}
	
	document.getElementById("area" + i).href = "";
	
}

window.onload = function(){
	var canvas = document.getElementById('myCanvas');
    var context = canvas.getContext('2d');
    
    var territories_by_player = document.getElementById("territories_by_player").value
    territories_by_player = territories_by_player.substring(1, territories_by_player.length-1).split(", ");
    
    var nb_soldiers_on_territory = document.getElementById("nb_soldiers_on_territory").value
    nb_soldiers_on_territory = nb_soldiers_on_territory.substring(1, nb_soldiers_on_territory.length-1).split(", ");
	
	for (var i = 0; i < NB_TERRITORIES; ++i){
		var x0 = Math.floor(ORIGINAL_COORDINATES[i][0] / original_width * canvas_width);
		var y0 = Math.floor(ORIGINAL_COORDINATES[i][1] / original_height * canvas_height);
		var x1 = Math.floor(ORIGINAL_COORDINATES[i][2] / original_width * canvas_width);
		var y1 = Math.floor(ORIGINAL_COORDINATES[i][3] / original_height * canvas_height);
	
		document.getElementById("area" + i).setAttribute("coords", x0 + "," + y0 + "," + x1 + "," + y1);
		document.getElementById("area" + i).setAttribute("alt", "alt");
		
		
		var centerX = (x0 + x1) / 2 + 5;
	    var centerY = (y0 + y1) / 2 + 5;
	    var radius = 10;

	    context.beginPath();
	    context.arc(centerX, centerY, radius, 0, 2 * Math.PI, false);
	    context.fillStyle = COLOR[parseInt(territories_by_player[i])];
	    context.fill();
	    context.lineWidth = 0.25;
	    context.strokeStyle = COLOR[parseInt(territories_by_player[i])];
	    context.stroke();

	    context.font = '8pt Calibri';
	    context.fillStyle = 'white';
	    context.textAlign = 'center';
	    context.fillText(parseInt(nb_soldiers_on_territory[i]), centerX, centerY + 3);
	    
	    battle_status = document.getElementById("battle_status").value;
	    if (battle_status == STATUS_ACTIVE_FOR_FORTIFICATION && document.getElementById("chosen_territory").value == ""){
	    	document.getElementById("number").style.display='none';
	    }
	}
}

function click(i, player){
	battle_status = document.getElementById("battle_status").value;
	
	if (battle_status == STATUS_ACTIVE_FOR_DISTRIBUTION){
		document.getElementById("chosen_territory").value = i;
		var number = document.getElementById("number").value;
		if (number > 0){
			document.getElementById("submit_for_distribution").href = "Risk?action=ADD_MANY&player=" + player + "&territory=" + i + "&number=" + number;
		}
		return;
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_ATTACK){
		if (document.getElementById("chosen_territory").value != ""){
			document.getElementById("chosen_territory_to").value = i;
			document.getElementById("submit_for_attack").href = "Risk?action=ATTACK&player=" + player + "&territory_from=" + document.getElementById("chosen_territory").value + "&territory_to=" + i;
		} else {
			document.getElementById("chosen_territory").value = i;
			document.getElementById("chosen_territory_to").value = "";
			document.getElementById("submit_for_attack").href = "";
		}
		return;
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_FORTIFICATION){
		if (document.getElementById("chosen_territory").value != ""){
			document.getElementById("chosen_territory_to").value = i;
			var number = document.getElementById("number").value;
			if (number > 0){
				document.getElementById("submit_for_fortification").href = "Risk?action=FORTIFY&player=" + player + "&territory_from=" + document.getElementById("chosen_territory").value + "&territory_to=" + i + "&number=" + number;
			}
		} else {
			document.getElementById("chosen_territory").value = i;
			document.getElementById("chosen_territory_to").value = "";
			document.getElementById("submit_for_attack").href = "";
		}
		return;
	}
}

function notify(){
	var battle_status = document.getElementById("battle_status").value;
	var number = document.getElementById("number").value;
	
	if (battle_status == STATUS_ACTIVE_FOR_DISTRIBUTION){
		document.getElementById("notification").innerHTML  = "Click on a territory you have to move " + number + " soldiers to."
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_FORTIFICATION){
		document.getElementById("notification").innerHTML  = "Click on a territory you have to move " + number + " soldiers to."
	}
}

function updowncard(player, card){
	var src = document.getElementById("card" + card).src;
	var battle_status = document.getElementById("battle_status").value;
	if (src.endsWith('Img/CA.png')){
		document.getElementById("card" + card).src = 'Img/C' + card + '.png'
	}
	
	if (battle_status == STATUS_ACTIVE_FOR_TUNE_IN){
		var card_1 = document.getElementById("tune_in_card_1").value;
		var card_2 = document.getElementById("tune_in_card_2").value;
		var card_3 = document.getElementById("tune_in_card_3").value;		
		
		if (card_3 != ''){
			document.getElementById("card" + card_1).style.opacity = 1;
			document.getElementById("tune_in_card_1").value = '';
			document.getElementById("card" + card_2).style.opacity = 1;
			document.getElementById("tune_in_card_2").value = '';
			document.getElementById("card" + card_3).style.opacity = 1;
			document.getElementById("tune_in_card_3").value = '';
			document.getElementById("card" + card).style.opacity = 0.5;
			document.getElementById("tune_in_card_1").value = card;
			document.getElementById("submit_for_tune_in").href = '';
		}
		
		else if (card_2 != ''){
			if (card != card_2){
				document.getElementById("card" + card).style.opacity = 0.5;
				document.getElementById("tune_in_card_3").value = card;
				document.getElementById("submit_for_tune_in").href = "Risk?action=TUNE_IN&player=" + player + "&card1=" + card_1 + "&card2=" + card_2 + "&card3=" + card;
			}
		}
		
		else if (card_1 != ''){
			if (card != card_1){
				document.getElementById("card" + card).style.opacity = 0.5;
				document.getElementById("tune_in_card_2").value = card;
				document.getElementById("submit_for_tune_in").href = '';
			}
		}
		
		else{
			document.getElementById("card" + card).style.opacity = 0.5;
			document.getElementById("tune_in_card_1").value = card;
			document.getElementById("submit_for_tune_in").href = '';
		}
		
	}
	
	else if (!src.endsWith('Img/CA.png')){
		document.getElementById("card" + card).src = 'Img/CA.png';
	}
}

function updownmission(mission){
	var src = document.getElementById("mission").src;
	
	if (src.endsWith('Img/MA.png')){
		document.getElementById("mission").src = 'Img/M' + mission + '.png'
	} else {
		document.getElementById("mission").src = 'Img/MA.png';
	}
}