package org.kitteh.tag;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import com.google.common.base.Preconditions;

public class PlayerReceiveNameTagEvent extends PlayerEvent
{

    private static final HandlerList handlers = new HandlerList();
    /*========================================================================*/
    private final Player namedPlayer;
    private String tag;
    private boolean modified;

    public PlayerReceiveNameTagEvent(Player who, Player namedPlayer, String initialName)
    {
        super( who );
        Preconditions.checkNotNull( who, "who" );
        Preconditions.checkNotNull( namedPlayer, "namedPlayer" );
        Preconditions.checkNotNull( initialName, "initialName" );

        this.namedPlayer = namedPlayer;
        this.tag = initialName;
    }
    
    public boolean setTag(String tag)
    {
        Preconditions.checkNotNull( tag, "tag" );

        this.tag = tag;
        this.modified = true;

        return tag.length() < 16;
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
     * Get the player whose nametag we're receiving
     *
     * @return the Player whose name is being affected
     */
    public Player getNamedPlayer() {
        return this.namedPlayer;
    }

    /**
     * Get the nametag that will be sent
     *
     * @return String nametag that will be sent
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * Has the event been modified yet?
     * <p>
     * Excellent method for plugins wishing to be rather passive
     *
     * @return true if the event has had the tag modified
     */
    public boolean isModified() {
        return this.modified;
    }

}
