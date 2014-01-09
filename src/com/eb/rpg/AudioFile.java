package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class AudioFile implements Marshallable {
	
	public String name = "";
	public int volume = 100, pitch = 100;

	public AudioFile() { }
	
	public AudioFile(String name) { this.name = name; }
	
	public AudioFile(String name, int volume) {
		this.name = name;
		this.volume = volume;
	}
	
	public AudioFile(String name, int volume, int pitch) {
		this.name = name;
		this.volume = volume;
		this.pitch = pitch;
	}

}
