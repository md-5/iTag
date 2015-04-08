package net.md_5.itag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

public class iTag extends JavaPlugin implements Listener
{

    @Getter
    private static iTag instance;
    private TagAPI tagAPI;
    private Map<Integer, Player> entityIdMap;
    private static final int[] uuidSplit = new int[]
    {
        0, 8, 12, 16, 20, 32
    };

    @Override
    public void onEnable()
    {
        instance = this;
        entityIdMap = new HashMap<Integer, Player>();
        tagAPI = new TagAPI( this );

        for ( Player player : getServer().getOnlinePlayers() )
        {
            entityIdMap.put( player.getEntityId(), player );
        }

        getServer().getPluginManager().registerEvents( this, this );
        ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter( this, PacketType.Play.Server.PLAYER_INFO )
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                if ( event.getPacket().getPlayerInfoAction().read( 0 ) != PlayerInfoAction.ADD_PLAYER )
                {
                    return;
                }

                List<PlayerInfoData> newPlayerInfo = new ArrayList<PlayerInfoData>();
                for ( PlayerInfoData playerInfo : event.getPacket().getPlayerInfoDataLists().read( 0 ) )
                {
                    Player player;
                    if ( playerInfo == null || playerInfo.getProfile() == null || ( player = getServer().getPlayer( playerInfo.getProfile().getUUID() ) ) == null )
                    {
                        // Unknown Player
                        newPlayerInfo.add( playerInfo );
                        continue;
                    }

                    newPlayerInfo.add( new PlayerInfoData(
                            getSentName( player.getEntityId(), playerInfo.getProfile(), event.getPlayer() ),
                            playerInfo.getPing(), playerInfo.getGameMode(), playerInfo.getDisplayName()
                    ) );
                }

                event.getPacket().getPlayerInfoDataLists().write( 0, newPlayerInfo );
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

        entityIdMap.clear();
        entityIdMap = null;
        tagAPI = null;
        instance = null;
    }

    private WrappedGameProfile getSentName(int sentEntityId, WrappedGameProfile sent, Player destinationPlayer)
    {
        Preconditions.checkState( getServer().isPrimaryThread(), "Can only process events on main thread." );

        Player namedPlayer = entityIdMap.get( sentEntityId );
        if ( namedPlayer == null )
        {
            // They probably were dead when we reloaded
            return sent;
        }

        PlayerReceiveNameTagEvent oldEvent = new PlayerReceiveNameTagEvent( destinationPlayer, namedPlayer, sent.getName() );
        getServer().getPluginManager().callEvent( oldEvent );

        StringBuilder builtUUID = new StringBuilder();
        if ( !sent.getId().contains( "-" ) )
        {
            for ( int i = 0; i < uuidSplit.length - 1; i++ )
            {
                builtUUID.append( sent.getId().substring( uuidSplit[i], uuidSplit[i + 1] ) ).append( "-" );
            }
        } else
        {
            builtUUID.append( sent.getId() );
        }
        AsyncPlayerReceiveNameTagEvent newEvent = new AsyncPlayerReceiveNameTagEvent( destinationPlayer, namedPlayer, oldEvent.getTag(), UUID.fromString( builtUUID.toString() ) );
        getServer().getPluginManager().callEvent( newEvent );

        return new WrappedGameProfile( newEvent.getUUID(), newEvent.getTag().substring( 0, Math.min( newEvent.getTag().length(), 16 ) ) );
    }

    public void refreshPlayer(Player player)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );

        for ( Player playerFor : player.getWorld().getPlayers() )
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
                    forWhom.showPlayer( player );
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
