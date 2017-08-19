import java.util.Arrays;

public class Test {
	public static void main (String[] args){
		State state = new State(3);

		System.out.println(state);
		
		System.out.println("---------");
		for (int i = 0; i < 21; i++){
			System.out.println("Round " + i);
			state.addOne(0, state.getTerritoriesOccupedBy(0).get((i % 14)));
			state.addOne(1, state.getTerritoriesOccupedBy(1).get((i % 8)));
			state.addOne(2, state.getTerritoriesOccupedBy(2).get((i % 9)));
		}
		for (int j = 0; j < 70; j++){
			for (int i = 0; i < 3; i++){
				System.out.println("---------FINISH FIRST DISTRIBUTION------");
				//state.tuneIn(i, 0);
				System.out.println("---------FINISH TUNEIN DISTRIBUTION------");
				state.addMany(i, state.getTerritoriesOccupedBy(i).get(0), state.available_soldiers_to_add[i]);
				System.out.println("---------FINISH DISTRIBUTION------");
				state.keepOnAttacking(i, state.getTerritoriesOccupedBy(i).get(0), state.getTerritoriesOccupedBy((i+1) % 3).get(0));
				state.keepOnAttacking(i, state.getTerritoriesOccupedBy(i).get(5), state.getTerritoriesOccupedBy((i+2) % 3).get(5));
				state.finishAttack(i);
				System.out.println("---------FINISH ATTACK-----------");
				state.fortify(i, state.getTerritoriesOccupedBy(i).get(1), state.getTerritoriesOccupedBy(i).get(0), 2);
				state.finishFortification(i);
				System.out.println(state);
			}
		}
		
		
	}
}
