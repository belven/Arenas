package belven.arena.arenas;

import belven.arena.phases.Phase;

public interface Phaseable {

	public void PhaseChanged(Phase p);

	public int getDifficulty();

}
