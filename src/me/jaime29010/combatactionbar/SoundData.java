package me.jaime29010.combatactionbar;

import org.bukkit.Sound;

public class SoundData {
	private final Sound sound;
	private final float volume, pitch;
	public SoundData(Sound sound, float volume, float pitch) {
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
}
