package org.kitteh.tag;

import java.util.Set;

import net.md_5.itag.iTag;

import org.bukkit.entity.Player;

public class TagAPI extends iTag
{
    public static void refreshPlayer(Player player)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshiTagPlayer( player );
        }
    }

    public static void refreshPlayer(Player player, Player forWhom)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshiTagPlayer( player, forWhom );
        }
    }

    public static void refreshPlayer(Player player, Set<Player> forWhom)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshiTagPlayer( player, forWhom );
        }
    }
}
