package com.eb.rpg;

import com.eb.rpg.builtin.Table;
import com.jinoh.ruby.marshal.Marshallable;

public class Tileset implements Marshallable {

	public String tileset_name = "",
			autotile_names[] = new String[]{"","","","","","",""},
			panorama_name = "";
	public int panorama_hue = 0;
	public String fog_name = "";
	public int fog_hue = 0, fog_opacity = 64, fog_blend_type = 0,
			fog_zoom = 200, fog_sx = 0, fog_sy = 0;
	public String battleback_name = "";
	public Table passages = new Table(384, 1, 1),
			priorities = new Table(384, 1, 1),
			terrain_tags = new Table(384, 1, 1);
	
	public Tileset () {
		priorities.setValueAt(0, 0, 0, 5);
	}

}
