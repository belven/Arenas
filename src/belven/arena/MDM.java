package belven.arena;

import java.util.List;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

/**
 * Meta Data Manager
 */
public class MDM {
	public static final String ArenaMob = "ArenaMob";
	public static final String ArenaAreaBlock = "ArenaAreaBlock";
	public static final String ChallengeBlock = "ChallengeBlock";
	public static final String RewardBoss = "RewardBoss";
	public static final String ArenaBlock = "ArenaBlock";
	public static final String ArenaBoss = "ArenaBoss";

	public static List<MetadataValue> getMetaData(String name, Metadatable m) {
		if (m.hasMetadata(name)) {
			List<MetadataValue> currentMetaData = m.getMetadata(name);

			return currentMetaData;
		}
		return null;
	}
}
