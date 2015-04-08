package org.kitteh.tag;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Delegate;
import lombok.Getter;
import net.md_5.itag.iTag;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;

public class TagAPI extends PluginBase implements PluginLoader
{

    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    public Pattern[] getPluginFileFilters()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin)
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    public void enablePlugin(Plugin plugin)
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    private interface Excludes
    {

        PluginLoader getPluginLoader();

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
            throw Throwables.propagate( ex );
        }

        plugins.add( this );
        lookupNames.put( getName(), this );
    }

    public PluginLoader getPluginLoader()
    {
        return this;
    }

    public void disablePlugin(Plugin plugin)
    {
        plugins.remove( plugin );
        lookupNames.remove( plugin.getName() );
    }

    private static Object getObj(Class<?> clazz, Object owner, String name)
    {
        try
        {
            Field field = clazz.getDeclaredField( name );
            field.setAccessible( true );
            return field.get( owner );
        } catch ( Exception ex )
        {
            throw Throwables.propagate( ex );
        }
    }

    public static void refreshPlayer(Player player)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshPlayer( player );
        }
    }

    public static void refreshPlayer(Player player, Player forWhom)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshPlayer( player, forWhom );
        }
    }

    public static void refreshPlayer(Player player, Set<Player> forWhom)
    {
        if ( iTag.getInstance() != null )
        {
            iTag.getInstance().refreshPlayer( player, forWhom );
        }
    }
}
