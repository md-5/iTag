package org.kitteh.tag;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerReceiveNameTagEvent extends PlayerEvent
{

    private static final HandlerList handlers = new HandlerList();
    /*========================================================================*/
    @Getter
    private final Player namedPlayer;
    @Getter
    private String tag;
    @Getter
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

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
