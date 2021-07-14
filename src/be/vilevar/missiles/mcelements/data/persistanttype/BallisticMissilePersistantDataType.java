package be.vilevar.missiles.mcelements.data.persistanttype;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
import be.vilevar.missiles.mcelements.data.MIRVData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BallisticMissilePersistantDataType implements PersistentDataType<byte[], BallisticMissileData> {

	public static final BallisticMissilePersistantDataType BALLISTIC_MISSILE = new BallisticMissilePersistantDataType();
	public static final NamespacedKey BALLISTIC_MISSILE_KEY = new NamespacedKey(Main.i, "ballistic-missile");
	
	@Override
	public Class<BallisticMissileData> getComplexType() {
		return BallisticMissileData.class;
	}

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}
	
	@Override
	public BallisticMissileData fromPrimitive(byte[] bytes, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.copiedBuffer(bytes);
		int stages = buffer.readInt();
		int[] impulses = new int[stages];
		int[] nFuels = new int[stages];
		int[] ejects = new int[stages];
		for(int i = 0; i < stages; i++) {
			impulses[i] = buffer.readInt();
			nFuels[i] = buffer.readInt();
			ejects[i] = buffer.readInt();
		}
		return new BallisticMissileData(stages, impulses, nFuels, ejects, buffer.readInt() == 1 ? MIRVData.readFrom(buffer) : null);
	}

	@Override
	public byte[] toPrimitive(BallisticMissileData missile, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.buffer();
		int stages = missile.getStages();
		buffer.writeInt(stages);
		for(int i = 0; i < stages; i++) {
			buffer.writeInt(missile.getImpulse(i));
			buffer.writeInt(missile.getNFuel(i));
			buffer.writeInt(missile.getEject(i));
		}
		if(missile.getWarhead() == null) {
			buffer.writeInt(0);
		} else {
			buffer.writeInt(1);
			missile.getWarhead().saveIn(buffer);
		}
		return buffer.array();
	}

}
