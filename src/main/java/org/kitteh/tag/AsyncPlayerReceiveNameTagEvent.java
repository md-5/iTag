package org.kitteh.tag;

import com.google.common.base.Preconditions;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import net.md_5.itag.profiles.ProfileUtils;
import net.md_5.itag.profiles.ProfileUtils.PlayerProfile;

public class AsyncPlayerReceiveNameTagEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();
    /*========================================================================*/
    @Getter
    private final Player player;
    @Getter
    private final Player namedPlayer;
    @Getter
    private String tag;
    @Getter
    private UUID UUID;
    @Getter
    private boolean tagModified;
    @Getter
    private boolean UUIDModified;
    @Getter
    private UUID skin;
    private boolean skinSet;

    public AsyncPlayerReceiveNameTagEvent(Player who, Player namedPlayer, String initialName, UUID uuid)
    {
        Preconditions.checkNotNull( who, "who" );
        Preconditions.checkNotNull( namedPlayer, "namedPlayer" );
        Preconditions.checkNotNull( initialName, "initialName" );
        Preconditions.checkNotNull( uuid, "uuid" );

        this.player = who;
        this.namedPlayer = namedPlayer;
        this.tag = initialName;
        this.tagModified = namedPlayer.getName().equals( initialName );
        this.UUID = uuid;
        this.skin = uuid;
    }

    public boolean setTag(String tag)
    {
        Preconditions.checkNotNull( tag, "tag" );
        //Backwards compatibility with skins -- Change skin when tag changes
        if (!skinSet) {
            Player p = Bukkit.getPlayerExact(tag);
            PlayerProfile profile;
            if (p == null) {
                profile = ProfileUtils.lookup(tag);
            } else {
                profile = new PlayerProfile(p.getUniqueId(), p.getName());
            }
            if (profile != null) {
                setSkin(profile.getId());
            }
        }
        this.tag = tag;
        this.tagModified = true;

        return tag.length() < 16;
    }

    public void setUUID(UUID uuid)
    {
        Preconditions.checkNotNull( uuid, "uuid" );

        this.UUID = uuid;
        this.UUIDModified = true;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    /**
     * Set the skin the player will see to the skin of another player
     * 
     * <p>
     * This method will probably block if the player isn't online
     * </p>
     * 
     * @throws IllegalArgumentException if the player with the specified uuid doesn't exist
     * @param id the uuid of the player whose skin you want the receiver to see
     */
    public void setSkin(UUID id) {
        if (Bukkit.getPlayer(id) == null) { //They aren't online so we have to check mojang
            PlayerProfile profile = ProfileUtils.lookup(id); //This caches so we really aren't wasting a lookup
            if (profile == null) throw new IllegalArgumentException("There is no player with the id " + id);
        }
        this.skin = id;
        skinSet = true;
    }
    
    /**
     * Set the skin the player will see to the skin of another player
     * 
     * <p>
     * WARNING: If the player isn't online, this method will block twice
     * </p>
     * 
     * @throws IllegalArgumentException if the player with the specified uuid doesn't exist
     * @param name the name of the player whose skin you want the receiver to see
     */
    public void setSkin(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p == null) {
            PlayerProfile profile = ProfileUtils.lookup(name);
            if (profile == null) throw new IllegalArgumentException("There is no player with the name " + name);
            setSkin(profile.getId());
        } else {
            setSkin(p.getUniqueId());
        }
    }
}
