package com.jinoh.ruby.marshal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Marshal {

	public static final byte[] VERSION_INFO = new byte[] {0x04, 0x08};
	public static final Map<Symbol, Class<?>> sSymbolToClass = new HashMap<Symbol, Class<?>>();
	public static final Map<Class<?>, Symbol> sClassToSymbol = new HashMap<Class<?>, Symbol>();
	public static final String[] KEYWORDS = new String[] {"abstract", "continue", "for", "new", "switch",
		"assert", "default", "goto", "package", "synchronized",
		"boolean", "do", "if", "private", "this",
		"break", "double", "implements", "protected", "throw",
		"byte", "else", "import", "public", "throws",
		"case", "enum", "instanceof", "return", "transient",
		"catch", "extends", "int", "short", "try",
		"char", "final", "interface", "static", "void",
		"class", "finally", "long", "strictfp", "volatile",
		"const", "float", "native", "super", "while"};
	
	static {
		register(new Object[] {
				"RPG::Actor",						com.eb.rpg.Actor.class,
				"RPG::Animation",					com.eb.rpg.Animation.class,
				"RPG::Animation::Frame",			com.eb.rpg.Animation.Frame.class,
				"RPG::Animation::Timing",			com.eb.rpg.Animation.Timing.class,
				"RPG::Armor",						com.eb.rpg.Armor.class,
				"RPG::AudioFile",					com.eb.rpg.AudioFile.class,
				"RPG::Class",						com.eb.rpg.ClassActor.class,
				"RPG::Class::Learning",			com.eb.rpg.ClassActor.Learning.class,
				"RPG::CommonEvent",					com.eb.rpg.CommonEvent.class,
				"RPG::Enemy",						com.eb.rpg.Enemy.class,
				"RPG::Enemy::Action",				com.eb.rpg.Enemy.Action.class,
				"RPG::Event",						com.eb.rpg.Event.class,
				"RPG::Event::Page",					com.eb.rpg.Event.Page.class,
				"RPG::Event::Page::Condition",	com.eb.rpg.Event.Page.Condition.class,
				"RPG::Event::Page::Graphic",		com.eb.rpg.Event.Page.Graphic.class,
				"RPG::EventCommand",				com.eb.rpg.EventCommand.class,
				"RPG::Item",							com.eb.rpg.Item.class,
				"RPG::Map",							com.eb.rpg.Map.class,
				"RPG::MapInfo",						com.eb.rpg.MapInfo.class,
				"RPG::MoveCommand",					com.eb.rpg.MoveCommand.class,
				"RPG::MoveRoute",					com.eb.rpg.MoveRoute.class,
				"RPG::Skill",						com.eb.rpg.Skill.class,
				"RPG::State",						com.eb.rpg.State.class,
				"RPG::System",						com.eb.rpg.System.class,
				"RPG::System::TestBattler",		com.eb.rpg.System.TestBattler.class,
				"RPG::System::Words",				com.eb.rpg.System.Words.class,
				"RPG::Tileset",						com.eb.rpg.Tileset.class,
				"RPG::Troop",						com.eb.rpg.Troop.class,
				"RPG::Troop::Member",				com.eb.rpg.Troop.Member.class,
				"RPG::Troop::Page",					com.eb.rpg.Troop.Page.class,
				"RPG::Weapon",						com.eb.rpg.Weapon.class,
				"Color",								com.eb.rpg.builtin.Color.class,
				"Table",								com.eb.rpg.builtin.Table.class,
		});
	}
	
	public static String ruby2field (Symbol symbol) {
		String t = symbol.toString();
		for (String s : KEYWORDS)
			if (t.equals(s))
				return t + "_";
		return t;
	}
	
	public static Symbol field2ruby (String name) {
		if (name.endsWith("_")) {
			String t = name.substring(0, name.length() - 1);
			for (String s : KEYWORDS)
				if (t.equals(s))
					return new Symbol(t);
		}
		return new Symbol(name);
	}
	
	public static <T> void register (T[] list) {
		Symbol s;
		Class<?> c;
		for (int i = 0; i < list.length; i += 2) {
			s = new Symbol(list[i].toString());
			c = (Class<?>) list[i+1];
			sSymbolToClass.put(s, c);
			sClassToSymbol.put(c, s);
		}
	}
	
	public static void register (Symbol name, Class<?> clazz) {
		sSymbolToClass.put(name, clazz);
		sClassToSymbol.put(clazz, name);
	}
	
	public static byte[] dump (Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dump (baos, obj);
		return baos.toByteArray();
	}
	
	public static void dump (OutputStream os, Object obj) throws IOException {
		os.write(VERSION_INFO);
		
		new Marshaler(os).marshalAuto(obj);
	}
	
	public static Object load (byte[] bytes) throws IOException {
		return loadAs (new ByteArrayInputStream(bytes), null);
	}
	
	public static Object load (InputStream is) throws IOException {
		return loadAs (is, null);
	}
	
	public static <T> T loadAs (byte[] bytes, Class<T> clazz) throws IOException {
		return loadAs (new ByteArrayInputStream(bytes), clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T loadAs (InputStream is, Class<T> clazz) throws IOException, ClassCastException {
		int major = is.read();
		int minor = is.read();
		
		if (major < 0 || minor < 0)
			throw new IOException("End of stream");
		
		if ((byte) major != VERSION_INFO[0] || (byte) minor > VERSION_INFO[1])
			throw new IOException("Incompatible version");
		
		return (T) (new Unmarshaler(is).unmarshalAuto(clazz));
	}
	

}
