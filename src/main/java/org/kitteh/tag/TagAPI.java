package org.kitteh.tag;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Set;

import net.md_5.itag.iTag;

import org.bukkit.entity.Player;

import com.google.common.base.Throwables;

public class TagAPI extends iTag
{
    public TagAPI()
    {

        StringWriter write = new StringWriter();
        String yaml = write.toString().replaceAll( "iTag", "TagAPI" );
    }

    private static Object getObj(Class<?> clazz, Object owner, String name)
    {
        try
        {
            Field field = clazz.getDeclaredField( name );
            field.setAccessible( true );
            return field.get( owner );
        } catch ( Throwable t )
        {
            Throwables.propagate( t );
        }

        // Impossible
        return null;
    }

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
