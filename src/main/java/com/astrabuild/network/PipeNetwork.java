package com.astrabuild.network;

import com.astrabuild.model.NetworkNode;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PipeNetwork {

    // Изолируем координаты от мусора Bukkit Location
    private record BlockPos(UUID worldId, int x, int y, int z) {
        static BlockPos from(Location loc) {
            return new BlockPos(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    private final Map<BlockPos, NetworkNode> graph = new ConcurrentHashMap<>();

    public void registerNode(Location loc, NetworkNode.NodeType type) {
        graph.put(BlockPos.from(loc), new NetworkNode(loc, type));
    }

    public void unregisterNode(Location loc) {
        graph.remove(BlockPos.from(loc));
    }

    public NetworkNode getNode(Location loc) {
        return graph.get(BlockPos.from(loc));
    }

    public PathResult findShortestPath(Location start) {
        BlockPos startPos = BlockPos.from(start);
        Queue<BlockPos> queue = new LinkedList<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        BlockPos targetPos = null;

        while (!queue.isEmpty()) {
            if (visited.size() > 500) break; // Hard limit for DDoS protection

            BlockPos current = queue.poll();
            NetworkNode node = graph.get(current);

            // Ищем коннектор
            if (node != null && node.type() == NetworkNode.NodeType.CONNECTOR_INSERT && !current.equals(startPos)) {
                targetPos = current;
                break;
            }

            for (BlockPos neighbor : getAdjacentPositions(current)) {
                if (!visited.contains(neighbor) && graph.containsKey(neighbor)) {
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (targetPos == null) return null;

        List<Location> path = new ArrayList<>();
        BlockPos curr = targetPos;
        while (curr != null) {
            NetworkNode node = graph.get(curr);
            if (node != null) path.add(node.location());
            curr = cameFrom.get(curr);
        }
        Collections.reverse(path);
        
        long travelTicks = path.size() * 5L; 
        return new PathResult(path, graph.get(targetPos).location(), travelTicks);
    }

    private List<BlockPos> getAdjacentPositions(BlockPos pos) {
        return List.of(
                new BlockPos(pos.worldId(), pos.x() + 1, pos.y(), pos.z()),
                new BlockPos(pos.worldId(), pos.x() - 1, pos.y(), pos.z()),
                new BlockPos(pos.worldId(), pos.x(), pos.y() + 1, pos.z()),
                new BlockPos(pos.worldId(), pos.x(), pos.y() - 1, pos.z()),
                new BlockPos(pos.worldId(), pos.x(), pos.y(), pos.z() + 1),
                new BlockPos(pos.worldId(), pos.x(), pos.y(), pos.z() - 1)
        );
    }

    public Set<Location> getConnectedNodes(Location start) {
        BlockPos startPos = BlockPos.from(start);
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<Location> connectedNodes = new HashSet<>();

        if (!graph.containsKey(startPos)) return connectedNodes;

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            if (visited.size() > 500) break; // Hard limit for DDoS protection

            BlockPos current = queue.poll();
            NetworkNode node = graph.get(current);

            if (node != null && !current.equals(startPos)) {
                connectedNodes.add(node.location());
            }

            for (BlockPos neighbor : getAdjacentPositions(current)) {
                if (!visited.contains(neighbor) && graph.containsKey(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return connectedNodes;
    }

    public record PathResult(List<Location> path, Location destination, long travelTimeTicks) {}
}