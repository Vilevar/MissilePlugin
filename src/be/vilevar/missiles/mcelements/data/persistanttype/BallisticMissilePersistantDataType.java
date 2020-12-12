package be.vilevar.missiles.mcelements.data.persistanttype;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.BallisticMissileData;
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
		return new BallisticMissileData(buffer.readFloat(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
				buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public byte[] toPrimitive(BallisticMissileData missile, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeFloat(missile.getExplosionPower());
		buffer.writeDouble(missile.getWeight());
		buffer.writeDouble(missile.getRotatingForce());
		buffer.writeDouble(missile.getRange());
		buffer.writeFloat(missile.getSpeed());
		buffer.writeDouble(missile.getFlightHeight());
		buffer.writeDouble(missile.getDetectorDistance());
		return buffer.array();
	}

}
