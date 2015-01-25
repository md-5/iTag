package net.md_5.itag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.base.Preconditions;

public class iTag extends JavaPlugin implements Listener
{
    private static iTag instance;
    private static final int[] uuidSplit = new int[]
    {
        0, 8, 12, 16, 20, 32
    };
    private SkinCache cache;
    
    //@Override
    public void onEnable()
    {
        instance = this;
        this.cache = new SkinCache();

        getServer().getPluginManager().registerEvents( this, this );
        ProtocolLibrary.getProtocolManager().addPacketListener( new PacketAdapter( this, PacketType.Play.Server.PLAYER_INFO, PacketType.Play.Server.NAMED_ENTITY_SPAWN)
        {
            public void onPacketSending(PacketEvent event)
            {
            	int clientVersion = ProtocolLibrary.getProtocolManager().getProtocolVersion(event.getPlayer());
            	if (clientVersion < 6 && event.getPacket().getType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            		event.getPacket().getGameProfiles().write( 0, getSentName(event.getPacket().getGameProfiles().read( 0 ), event.getPlayer() ) );
	                WrappedGameProfile profile = event.getPacket().getGameProfiles().read(0);
	                cache.changeSkin(event.getPlayer(), profile);
            	} else if (clientVersion == 47 && Bukkit.getVersion().contains("1.7") && event.getPacket().getType() == PacketType.Play.Server.PLAYER_INFO) {
            		if (!event.getPlayer().getName().equals(event.getPacket().getGameProfiles().read(0).getName())) {
	            		event.getPacket().getGameProfiles().write( 0, getSentName(event.getPacket().getGameProfiles().read( 0 ), event.getPlayer() ) );
		                WrappedGameProfile profile = event.getPacket().getGameProfiles().read(0);
		                cache.changeSkin(event.getPlayer(), profile);
            		}
            	} else if (clientVersion == 47 && event.getPacket().getType() == PacketType.Play.Server.PLAYER_INFO) {
            		if (event.getPacket().getPlayerInfoAction().getValues().get(0) == PlayerInfoAction.ADD_PLAYER) {
    	            	StructureModifier<List<PlayerInfoData>> infos = event.getPacket().getPlayerInfoDataLists();
    	            	List<PlayerInfoData> datas = new ArrayList<PlayerInfoData>();
    	            	for (PlayerInfoData data : infos.read(0)) {
    	            		if (data == null || data.getProfile() == null || Bukkit.getPlayer(data.getProfile().getUUID()) == null) {
    	            			continue;
    	            		}
    	            		
    	            		WrappedGameProfile profile = getSentName(data.getProfile(), event.getPlayer() );
    	            		
    	            		if (!profile.getName().endsWith(data.getProfile().getName()) && !event.getPlayer().getName().equals(data.getProfile().getName())) {
    	            			cache.changeSkin(event.getPlayer(), profile);
    		                    
    	            			datas.add(new PlayerInfoData(profile, data.getPing(), data.getGameMode(), data.getDisplayName()));
    	            		} else {
    	            			datas.add(data);
    	            		}
    	            		
    	            	}
    	            	infos.write(0, datas);
    	            	
                	}
            	}
            }	
        } );
    }

    //@Override
    public void onDisable()
    {
        ProtocolLibrary.getProtocolManager().removePacketListeners( this );
        instance = null;
    }

    private WrappedGameProfile getSentName(WrappedGameProfile sent, Player destinationPlayer)
    {
        Preconditions.checkState( getServer().isPrimaryThread(), "Can only process events on main thread." );

        Player namedPlayer = Bukkit.getPlayer(sent.getUUID());
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

    public void refreshiTagPlayer(Player player)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );

        for ( Player playerFor : player.getWorld().getPlayers() )
        {
        	refreshiTagPlayer( player, playerFor );
        }
    }

    public void refreshiTagPlayer(final Player player, final Player forWhom)
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

    public void refreshiTagPlayer(Player player, Set<Player> forWhom)
    {
        Preconditions.checkState( isEnabled(), "Not Enabled!" );
        Preconditions.checkNotNull( player, "player" );
        Preconditions.checkNotNull( forWhom, "forWhom" );

        for ( Player playerFor : forWhom )
        {
        	refreshiTagPlayer( player, playerFor );
        }
    }
    
    public static iTag getInstance() {
    	return instance;
    }
}
