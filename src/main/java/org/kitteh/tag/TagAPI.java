package org.kitteh.tag;

import com.google.common.base.Throwables;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.itag.iTag;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class TagAPI extends JavaPlugin
{

    private List<Plugin> plugins;
    private Map<String, Plugin> lookupNames;

    public void install(JavaPlugin parent)
    {
        plugins = (List<Plugin>) getObj( parent.getServer().getPluginManager(), "plugins" );
        lookupNames = (Map<String, Plugin>) getObj( parent.getServer().getPluginManager(), "lookupNames" );

        PluginDescriptionFile pdf = new PluginDescriptionFile( "TagAPI", parent.getDescription().getVersion(), getClass().getName() );
        initialize( parent.getPluginLoader(), parent.getServer(), pdf, parent.getDataFolder(), (File) getObj( parent, "file" ), getClass().getClassLoader() );

        plugins.add( this );
        lookupNames.put( getName(), this );

        setEnabled( true );
    }

    public void uninstall()
    {
        setEnabled( false );

        plugins.remove( this );
        lookupNames.remove( getName() );

        plugins = null;
        lookupNames = null;
    }

    private static Object getObj(Object owner, String name)
    {
        try
        {
            Field field = owner.getClass().getDeclaredField( name );
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
