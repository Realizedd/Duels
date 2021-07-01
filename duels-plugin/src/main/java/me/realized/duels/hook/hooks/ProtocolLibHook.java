package me.realized.duels.hook.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.InvocationTargetException;

public class ProtocolLibHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "ProtocolLib";

    private final ProtocolManager protocolManager;

    public ProtocolLibHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    private void sendPacket(PacketContainer container, Player player) {
        try {
            protocolManager.sendServerPacket(player, container);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet " + container, e);
        }
    }

    // WIP
    public void setInventoryTitle(Player player, Inventory inventory, String title) {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.OPEN_WINDOW);
        container.getModifier().writeDefaults();
        container.getIntegers().write(1, inventory.getSize()*9);
        container.getChatComponents().write(0, WrappedChatComponent.fromText(StringUtil.color(title)));
        sendPacket(container, player);

        PacketContainer container2 = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        container.getModifier().writeDefaults();
        container.getIntegers().write(1, inventory.getSize()*9);
        container.getChatComponents().write(0, WrappedChatComponent.fromText(StringUtil.color(title)));
        sendPacket(container, player);
    }

}
