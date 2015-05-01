package net.md_5.itag;

import java.lang.reflect.Method;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;

import org.bukkit.entity.Player;

public class Versions {
    private Versions() {}
    
    static {
        Method m = null;
        try {
            m = ProtocolManager.class.getDeclaredMethod("getProtocolVersion", Player.class);
        } catch (Exception e) {}
        GET_PROTOCOL_VERSION_METHOD = m;
    }
    
    private static final Method GET_PROTOCOL_VERSION_METHOD;
    
    public static int getProtocolVersion(PacketEvent event) {
        try {
            Player player = event.getPlayer();
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            return (Integer) GET_PROTOCOL_VERSION_METHOD.invoke(manager, player);
        } catch (Exception e) { //We must not be on protocol hack
            return MinecraftProtocolVersion.getCurrentVersion();
        }
    }
    
    public static SupportedVersion getSupportedVersion(PacketEvent event) {
        switch (getProtocolVersion(event)) {
            case 47 :
                return SupportedVersion.v1_8_0;
            case 5 :
                return SupportedVersion.v1_7_10;
            default :
                return SupportedVersion.UNSUPPORTED;
        }
    }
    
    public static enum SupportedVersion {
        v1_8_0,
        v1_7_10,
        UNSUPPORTED;
    }
}