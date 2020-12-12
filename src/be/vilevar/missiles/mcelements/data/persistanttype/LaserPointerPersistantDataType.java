package be.vilevar.missiles.mcelements.data.persistanttype;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.LaserPointerData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LaserPointerPersistantDataType implements PersistentDataType<byte[], LaserPointerData> {

	public static final LaserPointerPersistantDataType LASER_POINTER = new LaserPointerPersistantDataType();
	public static final NamespacedKey LASER_POINTER_KEY = new NamespacedKey(Main.i, "laser-pointer");
	
	@Override
	public Class<LaserPointerData> getComplexType() {
		return LaserPointerData.class;
	}

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}
	
	@Override
	public LaserPointerData fromPrimitive(byte[] bs, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.copiedBuffer(bs);
		double range = buffer.readDouble();
		Location t = null;
		if(buffer.readBoolean()) {
			t = new Location(Bukkit.getWorld(new UUID(buffer.readLong(), buffer.readLong())), buffer.readDouble(), buffer.readDouble(),
					buffer.readDouble());
		}
		return new LaserPointerData(range, t);
	}

	@Override
	public byte[] toPrimitive(LaserPointerData laser, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeDouble(laser.getRange());
		if(laser.getTarget() != null) {
			buffer.writeBoolean(true);
			Location t = laser.getTarget();
			buffer.writeLong(t.getWorld().getUID().getMostSignificantBits());
			buffer.writeLong(t.getWorld().getUID().getLeastSignificantBits());
			buffer.writeDouble(t.getX());
			buffer.writeDouble(t.getY());
			buffer.writeDouble(t.getZ());
		} else {
			buffer.writeBoolean(false);
		}
		return buffer.array();
	}

}
