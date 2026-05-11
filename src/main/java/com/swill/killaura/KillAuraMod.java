package com.swill.killaura;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KillAuraMod implements ModInitializer {
    
    private static boolean killAuraEnabled = false;
    private static final int RANGE = 3;
    private static final int ATTACK_COOLDOWN = 4;
    private static int tickCounter = 0;
    
    @Override
    public void onInitialize() {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.killaura.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.killaura"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            
            if (keyBinding.wasPressed()) {
                killAuraEnabled = !killAuraEnabled;
                System.out.println("[KillAura] " + (killAuraEnabled ? "ENABLED" : "DISABLED"));
            }
            
            if (!killAuraEnabled) return;
            
            tickCounter++;
            if (tickCounter < ATTACK_COOLDOWN) return;
            tickCounter = 0;
            
            PlayerEntity player = client.player;
            MinecraftClient mc = client;
            
            Box box = player.getBoundingBox().expand(RANGE);
            List<LivingEntity> entities = client.world.getEntitiesByClass(LivingEntity.class, box, e -> 
                e != player && e.isAlive() && !(e instanceof PlayerEntity && ((PlayerEntity)e).isCreative())
            );
            
            for (LivingEntity target : entities) {
                if (target instanceof PlayerEntity targetPlayer) {
                    if (targetPlayer.isCreative() || targetPlayer.isSpectator()) continue;
                }
                
                double distance = player.distanceTo(target);
                if (distance <= RANGE) {
                    mc.interactionManager.attackEntity(player, target);
                    player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        });
    }
}
