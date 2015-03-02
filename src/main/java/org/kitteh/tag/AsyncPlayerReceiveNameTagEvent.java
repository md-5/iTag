package org.kitteh.tag;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.google.common.base.Preconditions;

public class AsyncPlayerReceiveNameTagEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();
    /*========================================================================*/
    private final Player player;
    private final Player namedPlayer;
    private String tag;
    private UUID UUID;
    private boolean tagModified;
    private boolean UUIDModified;

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
    }

    public boolean setTag(String tag)
    {
        Preconditions.checkNotNull( tag, "tag" );

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

    //@Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    

    /**
     * Gets the player whose nametag we're receiving
     *
     * @return the Player whose name is being affected
     */
    public Player getNamedPlayer() {
        return this.namedPlayer;
    }

    /**
     * Gets the player receiving the tag
     *
     * @return the Player receiving the tag
     */
    public final Player getPlayer() {
        return player;
    }

    /**
     * Gets the nametag that will be sent
     *
     * @return nametag sent to the player
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * Gets the UUID that will be sent
     * <p>
     * Applies to MC version 1.7 and above
     * <p>
     * In prior versions it's a per-server UUID and not sent to the client
     *
     * @return uuid sent to the player
     */
    public UUID getUUID() {
        return this.UUID;
    }

    /**
     * Gets if the tag has been modified in this event
     *
     * @return true if the tag has been modified
     */
    public boolean isTagModified() {
        return this.tagModified;
    }

    /**
     * Gets if the UUID has been modified in this event
     *
     * @return true if the UUID has been modified
     */
    public boolean isUUIDModified() {
        return this.UUIDModified;
    }
}
