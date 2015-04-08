package net.md_5.itag.profiles;

import java.util.Iterator;
import java.util.UUID;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.md_5.itag.profiles.ProfileUtils.PlayerProfile;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TextureHelper {
    
    /**
     * Return the specified players skin property
     * <p>
     * If the player has a steve or alex skin this method will return null
     * </p>
     * 
     * @param id the players uuid
     * 
     * @throws IllegalArgumentException if the player with the given uuid doesn't exist
     * 
     * @return the players skin property or null
     */
    public static WrappedSignedProperty getSkin(UUID id) {
        WrappedGameProfile profile = getProfile(id);
        if (profile == null) throw new IllegalArgumentException("Player with " + id + " doesn't exist");
        Iterator<WrappedSignedProperty> properties = profile.getProperties().get("textures").iterator();
        if (!properties.hasNext()) return null; //Steve/Alex
        return properties.next();
    }
    
    private static WrappedGameProfile getProfile(UUID id) {
        if (Bukkit.getPlayer(id) != null) {
            return WrappedGameProfile.fromPlayer(Bukkit.getPlayer(id)); //Yay no lookup
        }
        PlayerProfile profile = ProfileUtils.lookup(id); //D:
        if (profile == null) return null;
        return toGameProfile(profile);
    }
    
    private static WrappedGameProfile toGameProfile(PlayerProfile playerProfile) {
        JSONArray jsonArray = playerProfile.getProperties();
        Multimap<String, WrappedSignedProperty> properties = null;
        if (jsonArray != null) {
            properties = ArrayListMultimap.create();
            for (Object obj : jsonArray) {
                if (!(obj instanceof JSONObject)) continue;
                JSONObject json = (JSONObject) obj;
                String name = (String) json.get("name");
                String value = (String) json.get("value");
                String signature = json.containsKey("signature") ? (String) json.get("signature") : null;
                WrappedSignedProperty property = WrappedSignedProperty.fromValues(name, value, signature);
                properties.put(name, property);
            }
        }
        WrappedGameProfile profile = new WrappedGameProfile(playerProfile.getId(), playerProfile.getName());
        if (properties != null) {
            profile.getProperties().clear(); //Just in case
            profile.getProperties().putAll(properties);
        }
        return profile;
    }
}