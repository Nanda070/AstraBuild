package com.astrabuild.model;

import org.bukkit.Location;

public record NetworkNode(Location location, NodeType type) {
    public enum NodeType {
        PIPE,
        CONNECTOR_EXTRACT,
        CONNECTOR_INSERT,
        MACHINE_QUARRY
    }
}