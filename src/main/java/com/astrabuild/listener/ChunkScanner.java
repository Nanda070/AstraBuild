package com.astrabuild.listener;

import com.astrabuild.model.NetworkNode;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.quarry.QuarryManager;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkScanner implements Listener {

    private final PipeNetwork network;
    private final QuarryManager quarryManager;
    private final MachineDataStorage storage;

    public ChunkScanner(PipeNetwork network, QuarryManager quarryManager, MachineDataStorage storage) {
        this.network = network;
        this.quarryManager = quarryManager;
        this.storage = storage;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        
        // Итерация только по блокам с датой, минимизация нагрузки
        for (BlockState state : chunk.getTileEntities()) {
            if (!(state instanceof TileState tileState)) continue;

            String type = storage.getMachineType(tileState.getBlock());
            if (type == null) continue;

            Location loc = tileState.getLocation();

            switch (type) {
                case "QUARRY" -> {
                    Location[] bounds = storage.getQuarryBoundsParsed(tileState.getBlock());
                    if (bounds != null) {
                        quarryManager.createAndStart(loc, bounds[0], bounds[1]);
                    }
                    network.registerNode(loc, NetworkNode.NodeType.MACHINE_QUARRY);
                }
                case "PIPE" -> network.registerNode(loc, NetworkNode.NodeType.PIPE);
                case "CONNECTOR_INSERT" -> network.registerNode(loc, NetworkNode.NodeType.CONNECTOR_INSERT);
                case "CONNECTOR_EXTRACT" -> network.registerNode(loc, NetworkNode.NodeType.CONNECTOR_EXTRACT);
                case "SATELLITE_TERMINAL" -> network.registerNode(loc, NetworkNode.NodeType.MACHINE_SATELLITE);
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        
        for (BlockState state : chunk.getTileEntities()) {
            if (!(state instanceof TileState)) continue;

            String type = storage.getMachineType(state.getBlock());
            if (type != null) {
                if (type.equals("QUARRY")) {
                    quarryManager.removeQuarry(state.getLocation());
                }
                network.unregisterNode(state.getLocation()); // Чистим граф от выгруженных узлов
            }
        }
    }
}