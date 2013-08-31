package net.md_5.itag;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class iTag extends JavaPlugin
{

    @Getter
    private static iTag instance;
    private TagAPI tagAPI;

    @Override
    public void onEnable()
    {
        instance = this;
        tagAPI = new TagAPI();
        tagAPI.install( this );

        ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter( this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.NAMED_ENTITY_SPAWN )
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                event.getPacket().getSpecificModifier( String.class ).write( 0, getSentName(
                        event.getPacket().getSpecificModifier( int.class ).read( 0 ),
                        event.getPacket().getSpecificModifier( String.class ).read( 0 ),
                        event.getPlayer() ) );
            }
        } );
    }

    @Override
    public void onDisable()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners( this );
        tagAPI.uninstall();

        tagAPI = null;
        instance = null;
    }

    String getSentName(int sentEntityId, String sentName, Player destinationPlayer)
    {
        Player namedPlayer = null;
        for ( Player player : getServer().getOnlinePlayers() )
        {
            if ( player.getEntityId() == sentEntityId )
            {
                namedPlayer = player;
                break;
            }
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

        if ( player != forWhom && player.getWorld() == forWhom.getWorld() && forWhom.canSee( player ) )
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
