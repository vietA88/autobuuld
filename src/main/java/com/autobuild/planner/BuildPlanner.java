package com.autobuild.planner;

import com.autobuild.schematic.SchematicLoader;
import java.util.*;
import java.util.stream.Collectors;

public class BuildPlanner {
    public static class BuildStep {
        public final int x, y, z;
        public final String blockState;
        public final int priority; // 0: Normal block, 1: Redstone/Piston, 2: Liquid
        public BuildStep(int x, int y, int z, String state, int priority) {
            this.x = x; this.y = y; this.z = z; this.blockState = state; this.priority = priority;
        }
    }

    public static Map<Integer, List<BuildStep>> planLayers(List<SchematicLoader.ParsedBlock> blocks, int anchorX, int anchorY, int anchorZ) {
        Map<Integer, List<BuildStep>> layers = new HashMap<>();

        for (SchematicLoader.ParsedBlock parsed : blocks) {
            int tx = anchorX + parsed.localX;
            int ty = anchorY + parsed.localY;
            int tz = anchorZ + parsed.localZ;
            int priority = getPriority(parsed.blockStateString);
            layers.computeIfAbsent(ty, k -> new ArrayList<>())
                    .add(new BuildStep(tx, ty, tz, parsed.blockStateString, priority));
        }

        for (List<BuildStep> layer : layers.values()) {
            sortLayer(layer);
        }
        return layers;
    }

    private static int getPriority(String state) {
        String lower = state.toLowerCase();
        if (lower.contains("water") || lower.contains("lava")) return 2;
        if (lower.contains("piston") || lower.contains("observer") || lower.contains("redstone") || lower.contains("repeater") || lower.contains("comparator")) return 1;
        return 0;
    }

    private static void sortLayer(List<BuildStep> steps) {
        steps.sort(Comparator.comparingInt(s -> s.priority));
        List<BuildStep> p0 = steps.stream().filter(s -> s.priority == 0).collect(Collectors.toList());
        List<BuildStep> p1 = steps.stream().filter(s -> s.priority == 1).collect(Collectors.toList());
        List<BuildStep> p2 = steps.stream().filter(s -> s.priority == 2).collect(Collectors.toList());
        steps.clear();
        steps.addAll(p0); steps.addAll(p1); steps.addAll(p2);
    }
}
