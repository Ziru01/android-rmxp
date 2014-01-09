package com.jinoh.ruby.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simple marshaler class for ruby marshal TODO: Support link, ivar,
 * and et-cetera, and vice-versa
 * 
 * @author jinoh67
 * 
 */
public class Marshaler {

	protected String mEncoding = "UTF-8";
	protected OutputStream mStream;

	protected ArrayList<Object> mCache = new ArrayList<Object>();
	protected ArrayList<Object> mSymCache = new ArrayList<Object>();
	
	public List<Object> getCache () { return mCache; }
	public List<Object> getSymCache () { return mSymCache; }

	public Marshaler(OutputStream os) {
		mStream = os;
	}

	public void setEncoding(String encoding) {
		mEncoding = encoding;
	}

	public String getEncoding() {
		return mEncoding;
	}

	public void marshalAuto(Object o) throws IOException {
		if (o == null) {
			marshal();
		} else if (o instanceof Boolean) {
			marshal((Boolean) o);
		} else if (o instanceof Number) {
			Number n = (Number) o;
			double d = n.doubleValue();
			if (d > Math.floor(d))
				marshal(d);
			else
				marshal(n.longValue());
		} else if (o instanceof BigInteger) {
			marshal((BigInteger) o);
		} else if (o instanceof String) {
			marshal((String) o);
		} else if (o instanceof Symbol) {
			marshal((Symbol) o);
		} else if (o.getClass().isArray()) {
			marshal((Object[]) o);
		} else if (o instanceof List) {
			marshal((List<?>) o);
		} else if (o instanceof Map) {
			marshal((Map<?, ?>) o);
		} else if (o instanceof Marshallable) {
			marshal((Marshallable) o);
		} else if (o instanceof RubyIVar) {
			marshal((RubyIVar) o);
		} else
			throw new RuntimeException("not supported");
	}

	public void marshal() throws IOException {
		mStream.write('0');
	}

	public void marshal(boolean value) throws IOException {
		mStream.write(value ? 'T' : 'F');
	}

	public void marshal(int value) throws IOException {
		if (value > 1073741823 || value < -1073741824) {
			mStream.write('l');
			writeBigInteger((long) value);
		} else {
			mStream.write('i');
			writeInt(value);
		}
	}

	public void marshal(long value) throws IOException {
		if (value > 1073741823L || value < -1073741824L) {
			mStream.write('l');
			writeBigInteger(value);
		} else {
			mStream.write('i');
			writeInt((int) value);
		}
	}

	public void marshal(double value) throws IOException {
		mStream.write('f');
		writeRubyFloat(value);
	}

	public void marshal(float value) throws IOException {
		mStream.write('f');
		writeRubyFloat(value);
	}

	public void marshal(BigInteger value) throws IOException {
		// FIXME: BigInteger in fixnum range..?
		mStream.write('l');
		writeBigInteger(value);
	}

	public void marshal(String value) throws IOException {
		mStream.write('"');
		writeString(value);
	}

	public void marshal(Object[] value) throws IOException {
		mStream.write('[');
		writeArray(value);
	}

	public void marshal(List<?> value) throws IOException {
		mStream.write('[');
		writeList(value);
	}

	public void marshal(Map<?, ?> value) throws IOException {
		mStream.write('{');
		writeHash(value);
	}

	public void marshal(Symbol value) throws IOException {
		mStream.write(':');
		writeSymbol(value);
	}

	public void marshal(RubyIVar o) throws IOException {
		mStream.write('I');
		writeIvar(o);
	}

	public void marshal(Marshallable value) throws IOException {
		if (value instanceof CustomMarshallable) {
			mStream.write('u');
			writeUserdef((CustomMarshallable) value);
		} else {
			mStream.write('o');
			writeFields(value);
		}
	}

	public boolean marshalLink(Object o) throws IOException {
		int i = mCache.indexOf(o);
		if (i < 0) {
			mCache.add(o);
			return false;
		}
		mStream.write('@');
		writeInt(i);
		return true;
	}

	public boolean marshalSymLink(Symbol o) throws IOException {
		int i = mSymCache.indexOf(o);
		if (i < 0) {
			mCache.add(o);
			return false;
		}
		mStream.write(';');
		writeInt(i);
		return true;
	}

	public void writeList(List<?> list) throws IOException {
		if (marshalLink(list))
			return;
		synchronized (list) {
			writeInt(list.size());
			for (Object o : list) {
				marshalAuto(o);
			}
		}
	}

	public void writeArray(Object[] list) throws IOException {
		if (marshalLink(list))
			return;
		writeInt(list.length);
		for (Object o : list) {
			marshalAuto(o);
		}
	}

	public void writeHash(Map<?, ?> map) throws IOException {
		if (marshalLink(map))
			return;
		writeInt(map.size());
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			marshalAuto(entry.getKey());
			marshalAuto(entry.getValue());
		}
	}

	public void writeIvar(RubyIVar ivar) throws IOException {
		if (marshalLink(ivar))
			return;
		marshalAuto(ivar.mValue);
		Map<Symbol, Object> map = ivar.mIvars;
		writeInt(map.size());
		for (Entry<Symbol, Object> entry : map.entrySet()) {
			marshal(entry.getKey());
			marshalAuto(entry.getValue());
		}
	}

	public void writeUserdef(CustomMarshallable value) throws IOException {
		if (marshalLink(value))
			return;
		Symbol s = Marshal.sClassToSymbol.get(value.getClass());
		if (s == null)
			throw new RuntimeException("Symbol not found!");
		marshal(s);
		writeBytesAsString(value.dump(this));
	}
	
	public void writeClassField(Marshallable value, Class<?> clazz) throws IOException, IllegalArgumentException, IllegalAccessException {
		Class<?> su = clazz.getSuperclass();
		if (su != null)
			writeClassField(value, su);
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			marshal(new Symbol(f.getName()));
			marshalAuto(f.get(value));
		}
	}

	public void writeFields(Marshallable value) throws IOException {
		if (marshalLink(value))
			return;
		Symbol s = Marshal.sClassToSymbol.get(value.getClass());
		if (s == null)
			throw new RuntimeException("Symbol not found!");
		marshal(s);
		try {
			writeClassField(value, value.getClass());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeSymbol(Symbol value) throws IOException {
		if (marshalSymLink(value))
			return;
		writeBytesAsString(value.toString().getBytes("UTF-8"));
	}

	public void writeString(String value) throws IOException {
		if (marshalLink(value))
			return;
		writeBytesAsString(value.getBytes(mEncoding));
	}

	public void writeBytesAsString(byte[] value) throws IOException {
		writeInt(value.length);
		mStream.write(value);
	}

	/**
	 * FIXME: this seems like its wrong and actually should support up to 8
	 * bytes, not 4
	 */
	public void writeInt(int value) throws IOException {
		if (value == 0) {
			mStream.write(0);
		} else if (0 < value && value < 123) {
			mStream.write(value + 5);
		} else if (-124 < value && value < 0) {
			mStream.write((value - 5) & 0xff);
		} else {
			byte[] buf = new byte[4];
			int i = 0;
			do {
				buf[i++] = (byte) (value & 0xff);
				value >>= 8;
			} while (i < buf.length && value != 0 && value != -1);
			mStream.write(value < 0 ? -i : i);
			mStream.write(buf, 0, i);
		}
	}

	public void writeBigInteger(long bigint) throws IOException {
		if (marshalLink(Long.valueOf(bigint)))
			return;
		mStream.write(bigint >= 0 ? '+' : '-');

		long absValue = Math.abs(bigint);

		byte[] digits;
		int size;
		if (bigint < 0x100L)
			size = 1;
		else if (bigint < 0x10000L)
			size = 2;
		else if (bigint < 0x1000000L)
			size = 3;
		else if (bigint < 0x100000000L)
			size = 4;
		else if (bigint < 0x10000000000L)
			size = 5;
		else if (bigint < 0x1000000000000L)
			size = 6;
		else if (bigint < 0x100000000000000L)
			size = 7;
		else
			size = 8;
		digits = new byte[size];
		for (int i = 0; i < size; ++i) {
			digits[i] = (byte) (absValue >> (i << 3));
		}

		boolean oddLengthNonzeroStart = (digits.length % 2 != 0 && digits[0] != 0);
		int shortLength = digits.length / 2;
		if (oddLengthNonzeroStart) {
			shortLength++;
		}
		writeInt(shortLength);

		for (int i = 0; i < shortLength * 2 && i < digits.length; i++) {
			mStream.write(digits[i]);
		}

		if (oddLengthNonzeroStart) {
			// Pad with a 0
			mStream.write(0);
		}
	}

	public void writeBigInteger(BigInteger bigint) throws IOException {
		if (marshalLink(bigint))
			return;
		mStream.write(bigint.signum() >= 0 ? '+' : '-');

		BigInteger absValue = bigint.abs();

		byte[] digits = absValue.toByteArray();

		boolean oddLengthNonzeroStart = (digits.length % 2 != 0 && digits[0] != 0);
		int shortLength = digits.length / 2;
		if (oddLengthNonzeroStart) {
			shortLength++;
		}
		writeInt(shortLength);

		for (int i = 1; i <= shortLength * 2 && i <= digits.length; i++) {
			mStream.write(digits[digits.length - i]);
		}

		if (oddLengthNonzeroStart) {
			// Pad with a 0
			mStream.write(0);
		}
	}

	public void writeRubyFloat(double d) throws IOException {
		if (marshalLink(Double.valueOf(d)))
			return;
		writeString(String.format(Locale.US, "%.16g", d));
	}
}
