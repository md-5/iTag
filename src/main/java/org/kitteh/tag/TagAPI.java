package org.kitteh.tag;

import com.google.common.base.Throwables;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Delegate;
import lombok.Getter;
import net.md_5.itag.iTag;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;

public class TagAPI extends PluginBase
{

    private interface Excludes
    {

        PluginDescriptionFile getDescription();

        String getName();
    }
    @Delegate(excludes = Excludes.class, types =
    {
        CommandExecutor.class, TabCompleter.class, Plugin.class
    })
    private final iTag parent;
    @Getter
    private PluginDescriptionFile description;
    private List<Plugin> plugins;
    private Map<String, Plugin> lookupNames;

    public TagAPI(iTag parent)
    {
        this.parent = parent;

        plugins = (List<Plugin>) getObj( SimplePluginManager.class, parent.getServer().getPluginManager(), "plugins" );
        lookupNames = (Map<String, Plugin>) getObj( SimplePluginManager.class, parent.getServer().getPluginManager(), "lookupNames" );

        StringWriter write = new StringWriter();
        parent.getDescription().save( write );
        String yaml = write.toString().replaceAll( "iTag", "TagAPI" );

        try
        {
            description = new PluginDescriptionFile( new StringReader( yaml ) );
        } catch ( InvalidDescriptionException ex )
        {
            Throwables.propagate( ex );
        }

        plugins.add( this );
        lookupNames.put( getName(), this );
    }

    public void uninstall()
    {
        plugins.remove( this );
        lookupNames.remove( getName() );

        plugins = null;
        lookupNames = null;
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
        iTag.getInstance().refreshPlayer( player );
    }

    public static void refreshPlayer(Player player, Player forWhom)
    {
        iTag.getInstance().refreshPlayer( player, forWhom );
    }

    public static void refreshPlayer(Player player, Set<Player> forWhom)
    {
        iTag.getInstance().refreshPlayer( player, forWhom );
    }
}
