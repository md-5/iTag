package org.kitteh.tag;

import com.google.common.base.Preconditions;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
