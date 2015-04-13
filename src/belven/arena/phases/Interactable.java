package belven.arena.phases;

import org.bukkit.entity.Player;

public abstract class Interactable {
	public static String metadataName = "Interactable";

	public abstract void interactedWith(Player p);
}
