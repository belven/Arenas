package belven.arena.timedevents;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageTimer extends BukkitRunnable {
	private List<Player> players = new ArrayList<Player>();
	private String messageToSend;

	public MessageTimer(List<Player> Players, String MessageToSend) {
		players = Players;
		messageToSend = MessageToSend;
	}

	@Override
	public void run() {
		for (Player p : players) {
			if (p != null) {
				p.sendMessage(messageToSend);
			}
		}
	}
}