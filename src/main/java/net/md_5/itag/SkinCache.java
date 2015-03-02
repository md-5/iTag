package net.md_5.itag;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

public class SkinCache {
    private Map<UUID, Collection<WrappedSignedProperty>> textures;
	private Object sessionService;
	private Method fillMethod;
    
    public SkinCache() {
    	this.textures = new HashMap<UUID, Collection<WrappedSignedProperty>>();
    	this.sessionService = getSessionService();
    	this.fillMethod = getFillMethod(sessionService);
    }
    
    public void changeSkin(Player player, WrappedGameProfile profile) {
    	profile.getProperties().putAll("textures", getSkin(profile.getName()));
    }
    
    public Collection<WrappedSignedProperty> getSkin(String playerName) {
    	playerName = ChatColor.stripColor(playerName);
    	try {
			return getSkin(UUIDFetcher.getUUIDOf(playerName), playerName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    public Collection<WrappedSignedProperty> getSkin(UUID id, String playerName) {
		if (!this.textures.containsKey(id)) {
			WrappedGameProfile profile = new WrappedGameProfile(id, playerName);
    		
    		Object handle = profile.getHandle();
    		try {
    			fillMethod.invoke(sessionService, handle, true);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
    		profile = WrappedGameProfile.fromHandle(handle);
    		this.textures.put(id, profile.getProperties().get("textures"));
		}
		return textures.get(id);
    }
    
    private Object getSessionService() {
        Server server = Bukkit.getServer();
        try {
            Object mcServer = server.getClass().getDeclaredMethod("getServer").invoke(server);
            for (Method m : mcServer.getClass().getMethods()) {
                if (m.getReturnType().getSimpleName().equalsIgnoreCase("MinecraftSessionService")) {
                    return m.invoke(mcServer);
                }
            }
        }
        catch (Exception ex) {
            throw new IllegalStateException("An error occurred while trying to get the session service", ex);
        }
        throw new IllegalStateException("No session service found :o");
    }
 
    private Method getFillMethod(Object sessionService) {
        for(Method m : sessionService.getClass().getDeclaredMethods()) {
            if(m.getName().equals("fillProfileProperties")) {
                return m;
            }
        }
        throw new IllegalStateException("No fillProfileProperties method found in the session service :o");
    }
}
