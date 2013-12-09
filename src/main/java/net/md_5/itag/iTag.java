package net.md_5.itag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class iTag extends JavaPlugin implements Listener
{

    @Getter
    private static iTag instance;
    private TagAPI tagAPI;
    private Map<Integer, Player> entityIdMap;

    @Override
    public void onEnable()
    {
        instance = this;
        entityIdMap = new HashMap<Integer, Player>();
        tagAPI = new TagAPI();
        tagAPI.install( this );

        for ( Player player : getServer().getOnlinePlayers() )
        {
            entityIdMap.put( player.getEntityId(), player );
        }

        getServer().getPluginManager().registerEvents( this, this );
        ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter( this, PacketType.Play.Server.NAMED_ENTITY_SPAWN )
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                event.getPacket().getSpecificModifier( String.class ).write( 1, getSentName(
                        event.getPacket().getSpecificModifier( int.class ).read( 0 ),
                        event.getPacket().getSpecificModifier( String.class ).read( 1 ),
                        event.getPlayer() ) );
            }
        } );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        entityIdMap.put( event.getPlayer().getEntityId(), event.getPlayer() );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        entityIdMap.remove( event.getPlayer().getEntityId() );
    }

    @Override
    public void onDisable()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners( this );
        tagAPI.uninstall();

        entityIdMap.clear();
        entityIdMap = null;
        tagAPI = null;
        instance = null;
    }

    private String getSentName(int sentEntityId, String sentName, Player destinationPlayer)
    {
        Player namedPlayer = entityIdMap.get( sentEntityId );
        if ( namedPlayer == null )
        {
            // They probably were dead when we reloaded
            return sentName;
        }

        final PlayerReceiveNameTagEvent event = new PlayerReceiveNameTagEvent( destinationPlayer, namedPlayer, sentName );
        if ( getServer().isPrimaryThread() )
        {
            getServer().getPluginManager().callEvent( event );
        } else
        {
            Futures.getUnchecked( getServer().getScheduler().callSyncMethod( this, new Callable<Void>()
            {
                public Void call() throws Exception
                {
                    getServer().getPluginManager().callEvent( event );
                    return null;
                }
            } ) );
        }

        return event.getTag().substring( 0, Math.min( event.getTag().length(), 16 ) );
    }

    public void refreshPlayer(Player player)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );

        for ( Player playerFor : getServer().getOnlinePlayers() )
        {
            refreshPlayer( player, playerFor );
        }
    }

    public void refreshPlayer(final Player player, final Player forWhom)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );
        Preconditions.checkNotNull( forWhom, "forWhom" );

        if ( player != forWhom && forWhom.canSee( player ) )
        {
            forWhom.hidePlayer( player );
            getServer().getScheduler().scheduleSyncDelayedTask( this, new Runnable()
            {
                public void run()
                {
                    if ( player.isOnline() && forWhom.isOnline() )
                    {
                        forWhom.showPlayer( player );
                    }
                }
            }, 2 );
        }
    }

    public void refreshPlayer(Player player, Set<Player> forWhom)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );
        Preconditions.checkNotNull( forWhom, "forWhom" );

        for ( Player playerFor : forWhom )
        {
            refreshPlayer( player, playerFor );
        }
    }
}
