package belven.arena.arenas;

import belven.arena.phases.Phase;

public interface Phaseable {
	private int phaseDifficulty = 1;

	public void PhaseChanged(Phase p);
	
	public int getPhaseDifficulty() {
		return phaseDifficulty;
	}
	
	public void setPhaseDifficulty(int difficulty) {
		this.phaseDifficulty = difficulty;
	}
}
