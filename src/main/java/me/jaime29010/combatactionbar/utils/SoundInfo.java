package me.jaime29010.combatactionbar.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundInfo {
    private final Sound sound;
    private final float volume, pitch;

    public SoundInfo(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}