package com.autobuild.inventory;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.screen.ChestScreen;
import net.minecraft.client.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class ChestGUIManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static ChestBlockEntity currentTargetChest = null;
    private static Item targetItem = null;
    private static int targetAmount = 0;
    private static int clickCount = 0;

    public static void startAutoWithdraw(ChestBlockEntity chest, Item item, int amount) {
        currentTargetChest = chest;
        targetItem = item;
        targetAmount = amount;
        clickCount = 0;
        openChest(chest);
    }

    private static void openChest(ChestBlockEntity chest) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;
        Vec3d hitCenter = new Vec3d(chest.getPos().getX() + 0.5, chest.getPos().getY() + 0.5, chest.getPos().getZ() + 0.5);
        client.getInteractionManager().interactBlock(
                player, client.world, Hand.MAIN_HAND, chest.getPos(),
                Direction.UP, hitCenter
        );
    }

    public static void tickAutoWithdraw() {
        if (currentTargetChest == null) return;
        Screen screen = client.currentScreen;
        if (screen instanceof ChestScreen chestScreen) {
            int slotIndex = -1;
            for (int i = 0; i < currentTargetChest.getInventory().size(); i++) {
                if (currentTargetChest.getInventory().getStack(i).isOf(targetItem)) {
                    slotIndex = i;
                    break;
                }
            }
            if (slotIndex != -1) {
                int slotX = chestScreen.getX() + (slotIndex % 9) * 18 + 9;
                int slotY = chestScreen.getY() + (slotIndex / 9) * 18 + 9;
                chestScreen.mouseClicked(slotX, slotY, 0);
                clickCount++;

                if (clickCount >= targetAmount) {
                    client.player.closeHandledScreen();
                    currentTargetChest = null;
                }
            } else {
                client.player.closeHandledScreen();
                currentTargetChest = null;
            }
        } else {
            clickCount++;
            if (clickCount > 20) {
                currentTargetChest = null;
            }
        }
    }
}
