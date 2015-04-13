package belven.arena.phases;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class PhaseBlock extends Interactable {
	private InteractionPhase phase;
	private BlockState block;

	public PhaseBlock(InteractionPhase phase, Block b) {
		setPhase(phase);
		setBlockState(b.getState());
	}

	public void SetMetaData() {
		getBlockState().setMetadata(Interactable.metadataName, new FixedMetadataValue(getPhase().getPlugin(), this));
		getBlockState().getBlock().setType(Material.DIAMOND_BLOCK);
	}

	public InteractionPhase getPhase() {
		return phase;
	}

	public void setPhase(InteractionPhase phase) {
		this.phase = phase;
	}

	public BlockState getBlockState() {
		return block;
	}

	public void setBlockState(BlockState blockState) {
		this.block = blockState;
	}

	@Override
	public void interactedWith(Player p) {
		getPhase().interactionOccured(this);
		getBlockState().update(true);
		getBlockState().removeMetadata(Interactable.metadataName, getPhase().getPlugin());

	}
}
