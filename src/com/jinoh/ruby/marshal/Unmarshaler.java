package com.jinoh.ruby.marshal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Unmarshaler {

	protected String mEncoding = "UTF-8";
	protected InputStream mStream;

	protected ArrayList<Object> mCache = new ArrayList<Object>();
	protected ArrayList<Object> mSymCache = new ArrayList<Object>();
	public List<Object> getCache () { return mCache; }
	public List<Object> getSymCache () { return mSymCache; }
	
	public Unmarshaler(InputStream is) {
		mStream = is;
	}
	
	public InputStream getInputStream () {
		return mStream;
	}

	public void setEncoding(String encoding) {
		mEncoding = encoding;
	}

	public String getEncoding() {
		return mEncoding;
	}
	
	public Object unmarshalAuto() throws IOException {
		return unmarshalAuto(mStream.read(), null);
	}
	
	public Object unmarshalAuto(int c) throws IOException {
		return unmarshalAuto(c, null);
	}
	
	public Object unmarshalAuto(Class<?> type) throws IOException {
		return unmarshalAuto(mStream.read(), type);
	}

	@SuppressWarnings("unchecked")
	public Object unmarshalAuto(int c, Class<?> type) throws IOException {
		switch (c) {
		case '0': // nil
			return null;
		case 'T': // true
			return true;
		case 'F': // false
			return false;
		case 'i':
			return readInt();
		case 'l':
			return readBigInteger();
		case '"':
			return readString();
		case ':':
			return readSymbol();
		case '[':
			if (type == null) return readArray();
			return readArray(type.getComponentType());
		case '{':
			try {
				return readHash(type.asSubclass(Map.class));
			} catch (Exception e) { // Either NullPointerException or ClassCastException
				return readHash();
			}
		case 'o':
			return readMarshalable();
		case 'u':
			return readUserdef();
		case 'f':
			return readRubyFloat();
		case '@':
			int i = readInt();
			if (i >= mCache.size())
				throw new IOException ("Invalid cache index");
			return mCache.get(i);
		case ';':
			i = readInt();
			if (i >= mSymCache.size())
				throw new IOException ("Invalid symbol cache index");
			return mSymCache.get(i);
		case 'I':
			return readIvar();
		}
		throw new RuntimeException("WTF?");
	}
	
	public Symbol forceUnmarshalSymbol() throws IOException {
		int c = mStream.read();
		if (c == ':' || c == ';')
			return (Symbol) unmarshalAuto(c);
		throw new IOException("Expected symbol, got " + ((c == -1) ? "EOF" : (char) c));
	}
	
	public Symbol readSymbol() throws IOException {
		Symbol o = new Symbol(new String(readBytesAsString(), "UTF-8"));
		mSymCache.add(o);
		return o;
	}
	
	public Object[] readArray() throws IOException {
		return (Object[]) readArray(Object.class);
	}
	
	public Object readArray(Class<?> clazz) throws IOException {
		int size = readInt();
		Object array = Array.newInstance(clazz, size);
		mCache.add(array);

		for (int i = 0; i < size; i++) {
			Array.set(array, i, unmarshalAuto());
		}

		return array;
	}
	
	public Map<?, ?> readHash() throws IOException {
		int size = readInt();
		Map<Object, Object> hash = new LinkedHashMap<Object, Object>();
		mCache.add(hash);

		for (int i = 0; i < size; i++) {
			Object key = unmarshalAuto();
			Object value = unmarshalAuto();

			hash.put(key, value);
		}

		return hash;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V, T extends Map<K, V>> T readHash(Class<T> clazz) throws IOException {
		int size = readInt();
		T hash;
		try {
			hash = clazz.getConstructor(int.class).newInstance(size);
		} catch (Exception e) {
			try {
				hash = clazz.getConstructor().newInstance();
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
		mCache.add(hash);

		for (int i = 0; i < size; i++) {
			K key = (K) unmarshalAuto();
			V value = (V) unmarshalAuto();

			hash.put(key, value);
		}

		return hash;
	}
	
	public Marshallable readMarshalable () throws IOException {
		Symbol objName = forceUnmarshalSymbol();
		
		Class<?> clazz = Marshal.sSymbolToClass.get(objName);
		if (clazz == null)
			throw new IOException ("Symbol " + objName + " not registered. Counts : " + Marshal.sSymbolToClass.size());
		
		try {
			Marshallable inst = (Marshallable) clazz.newInstance();
			mCache.add(inst);
			int fieldCount = readInt();
			String name;
			Field f;
			Class<?> cl;
			
			for (int i = 0; i < fieldCount; i++) {
				name = forceUnmarshalSymbol().toString();
				if (name.charAt(0) == '@') {
					name = name.substring(1);
					cl = clazz;
					Object val = null;
					boolean bRead = false;
					int oos = 0;
					do {
						try {
							f = cl.getDeclaredField(name);
							f.setAccessible(true);
							val = unmarshalAuto(f.getType());
							bRead = true;
							f.set(inst, val);
							break;
						} catch (NoSuchFieldException e) {
							cl = cl.getSuperclass();
						} catch (IllegalAccessException e) {
							break;
						}
						if (oos++ > 40)
							throw new RuntimeException("40+ deep / " + clazz + " > " + cl);
					} while (cl != null);
					if (!bRead)
						val = unmarshalAuto();
				}
			}
			
			return inst;
		} catch (RuntimeException e1) {
			throw e1;
		} catch (IOException e1) {
			throw e1;
		} catch (InstantiationException e1) {
			throw new RuntimeException(e1);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		}
	}
	
	public CustomMarshallable readUserdef() throws IOException {
		Symbol objName = forceUnmarshalSymbol();
		
		Class<?> clazz = Marshal.sSymbolToClass.get(objName);
		if (clazz == null)
			throw new IOException ("Symbol " + objName + " not registered.");
		
		try {
			CustomMarshallable inst = (CustomMarshallable) clazz.newInstance();
			mCache.add(inst);
			inst.load(this, readBytesAsString());
			return inst;
		} catch (RuntimeException e1) {
			throw e1;
		} catch (IOException e1) {
			throw e1;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public byte readByte() throws IOException {
		int c = mStream.read();
		if (c == -1) throw new IOException("End of stream");
		return (byte) c;
	}
	
	public int readUnsignedByte() throws IOException {
		return ((int) readByte()) & 0xFF;
	}
	
	public String readString(String encoding) throws IOException {
		String s = new String(readBytesAsString(), encoding);
		mCache.add(s);
		return s;
	}
	
	public byte[] readBytesAsString() throws IOException {
		byte[] buf = new byte[readInt()];
		if (mStream.read(buf) < buf.length) throw new IOException("End of stream");
		return buf;
	}
	
	public String readString() throws IOException {
		return readString(mEncoding);
	}
	
	public Object readIvar() throws IOException {
		int c = mStream.read();
		Map<Symbol, Object> map = new HashMap<Symbol, Object>();
		RubyIVar ivar = new RubyIVar(null, map);
		mCache.add(ivar);
		int count;
		Symbol name;
		Object o;
		String str;
		if (c == '"') { // it is a raw string, so encode is necessary
			byte[] data = readBytesAsString();
			int idx;
			synchronized (mCache) { // space for string
				idx = mCache.size();
				mCache.add(null);
			}
			count = readInt();
			String encoding = null;
			while (count-- > 0) {
				str = (name = forceUnmarshalSymbol()).toString();
				map.put(name, o = unmarshalAuto());
				if (str.equals("E")) {
					if (o instanceof Boolean) {
						encoding = ((Boolean) o).booleanValue() ? "UTF-8" : "US-ASCII";
					}
				} else if (str.equals("encoding")) {
					if (o instanceof String) {
						encoding = o.toString();
					}
				}
			}
			if (encoding != null) {
				str = new String(data, encoding);
			} else str = new String(data, mEncoding);
			ivar.setValue(str);
			mCache.set(idx, str);
			return ivar;
		}
		ivar.setValue(unmarshalAuto());
		count = readInt();
		while (count-- > 0) {
			str = (name = forceUnmarshalSymbol()).toString();
			map.put(name, o = unmarshalAuto());
		}
		return ivar;
	}

	public int readInt() throws IOException {
		int c = readUnsignedByte();
		if (c == 0) {
			return 0;
		} else if (5 < c && c < 128) {
			return c - 5;
		} else if (-129 < c && c < -5) {
			return c + 5;
		}
		int result;
		if (c > 0) {
			c <<= 3;
			result = 0;
			for (int i = 0; i < c; i += 8)
				result |= readUnsignedByte() << i;
		} else {
			c = (byte) ((-c) << 3);
			result = -1;
			for (int i = 0; i < c; i += 8)
				result = (result & ~(0xff << i)) | (readUnsignedByte() << i);
		}
		return result;
	}

	public Object readBigInteger() throws IOException {
		// what a convoluted way to serialize a big integer (gotta love ruby)
		boolean positive = readByte() == '+';
		int shortLength = readInt(), i;
		Object o;

		if (shortLength > 8) {
			// BigInteger required a sign byte in incoming array
			byte[] digits = new byte[(shortLength << 1) + 1];
	
			digits[0] = positive ? 0 : ((byte) -1);
			for (i = digits.length - 1; i > 0; i--) {
				digits[i] = readByte();
			}
			
			o = new BigInteger(digits);
		} else {
			long value = 0;
			shortLength <<= 4;
			for (i = 0; i < shortLength; i += 8) {
				value |= readByte() << i;
			}
			if (!positive) o = -value;
			o = value;
		}
		mCache.add(o);
		return o;
	}
	
	public double readRubyFloat () throws IOException {
		double val = Double.parseDouble(new String(readBytesAsString()));
		mCache.add(val);
		return val;
	}

}
