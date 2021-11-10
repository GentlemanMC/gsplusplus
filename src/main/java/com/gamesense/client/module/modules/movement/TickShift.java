package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.StringSetting;
import com.gamesense.api.util.misc.KeyBoardClass;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

@Module.Declaration(name = "TickShift", category = Category.Movement)
public class TickShift extends Module {

    IntegerSetting limit = registerInteger("Limit", 16, 1, 50);
    DoubleSetting timer = registerDouble("Timer", 2, 1, 5);
    BooleanSetting doDecay = registerBoolean("Decay", false);
    DoubleSetting min = registerDouble("Lowest", 1.4, 1, 5, () -> doDecay.getValue());
    StringSetting onClick = registerString("onClick", "");

    int ticks;

    @Override
    protected void onEnable() {
        mc.timer.tickLength = 50;
        ticks = 0;
    }

    @Override
    protected void onDisable() {
        mc.timer.tickLength = 50;
    }

    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer)
            if (!(event.getPacket() instanceof CPacketPlayer.Rotation))
                ticks--;

    });

    @Override
    public void onUpdate() {

        if (isMoving()) { // garunteed movement packet

            if (ticks > 0 && !PlayerUtil.isPlayerClipped()) {

                double steps = (timer.getValue() - min.getValue()) / limit.getValue();

                double ourTimer = !doDecay.getValue() ? timer.getValue() :
                        steps * ticks + min.getValue();

                String bind = onClick.getText();

                if (ticks > 0 && (bind.length() == 0 || Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(bind.charAt(0))))) {
                    mc.timer.tickLength = doDecay.getValue() ? (Math.max(50f / (float) ourTimer, 50f)) : 50 / timer.getValue().floatValue();
                }
            }

        } else {

            if (!MotionUtil.isMoving(mc.player)) {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            mc.timer.tickLength = 50;
            if (ticks < limit.getValue())
                ticks++;
        }

    }

    @Override
    public String getHudInfo() {
        return TextFormatting.WHITE + "[" + getColour(ticks) + ticks + TextFormatting.WHITE + "]";
    }

    public TextFormatting getColour(int ticks) {

        if (ticks == 0) {
            return TextFormatting.RED;
        } else if (ticks <= limit.getValue()) {
            return TextFormatting.GREEN;
        } else {
            return TextFormatting.GOLD;
        }

    }

    boolean isMoving() {

        return MotionUtil.getMotion(mc.player) + Math.abs(mc.player.posY - mc.player.prevPosY) != 0;

    }

}
