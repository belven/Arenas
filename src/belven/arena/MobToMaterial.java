package belven.arena;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MobToMaterial {
	public EntityType et;
	public Material m;

	public MobToMaterial(EntityType entityType, Material material) {
		et = entityType;
		m = material;
	}
}
