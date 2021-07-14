package be.vilevar.missiles.mcelements.data.persistanttype;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import be.vilevar.missiles.Main;
import be.vilevar.missiles.mcelements.data.MIRVData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MIRVPersistantDataType implements PersistentDataType<byte[], MIRVData> {

	public static final MIRVPersistantDataType MIRV = new MIRVPersistantDataType();
	public static final NamespacedKey MIRV_KEY = new NamespacedKey(Main.i, "mirv");
	
	@Override
	public Class<MIRVData> getComplexType() {
		return MIRVData.class;
	}

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}
	
	@Override
	public MIRVData fromPrimitive(byte[] bytes, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.copiedBuffer(bytes);
		return MIRVData.readFrom(buffer);
	}

	@Override
	public byte[] toPrimitive(MIRVData mirv, PersistentDataAdapterContext ctx) {
		ByteBuf buffer = Unpooled.buffer();
		mirv.saveIn(buffer);
		return buffer.array();
	}

}
