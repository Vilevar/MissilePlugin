package be.vilevar.missiles.mcelements.persistantdata;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.CustomElementManager.BalisticMissileData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BalisticMissilePersistantDataType implements PersistentDataType<byte[], BalisticMissileData> {

	public static final BalisticMissilePersistantDataType BALLISTIC_MISSILE = new BalisticMissilePersistantDataType();
	public static final NamespacedKey BALLISTIC_MISSILE_KEY = new NamespacedKey(Main.i, "ballistic-missile");
	
	@Override
	public Class<BalisticMissileData> getComplexType() {
		return BalisticMissileData.class;
	}

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}
	
	@Override
	public BalisticMissileData fromPrimitive(byte[] bytes, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.copiedBuffer(bytes);
		return new BalisticMissileData(buffer.readFloat(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
				buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public byte[] toPrimitive(BalisticMissileData missile, PersistentDataAdapterContext ctx) {
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
