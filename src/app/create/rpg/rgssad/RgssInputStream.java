package app.create.rpg.rgssad;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class RgssInputStream extends InputStream {

	/* 
	 int available() Returns an estimated number of bytes that can be read or skipped without blocking for more input.
	 void close() Closes this stream.
	 void mark(int readlimit) Sets a mark position in this InputStream.
	 boolean markSupported() Indicates whether this stream supports the mark() and reset()methods.
	 int read(byte[] buffer) Equivalent to read(buffer, 0, buffer.length).
	 abstract int read() Reads a single byte from this stream and returns it as an integer in the range from 0 to 255.
	 int read(byte[] buffer, int offset, int length) Reads at most length bytes from this stream and stores them in the byte array b starting at offset.
	 synchronized void reset() Resets this stream to the last marked location.
	 long skip(long byteCount) Skips at most n bytes in this stream.
	 */
	RgssEntry mEntry;
	long mOffset, mTotal;
	int mMagic;
	InputStream mStream;
	byte mVersion;
	// flag != 0x53534752 || flag1 != 0x01004441
	public static byte[] MAGIC = new byte[] {0x52, 0x47, 0x53, 0x53, 0x41, 0x44, 0x00};

	public RgssInputStream(InputStream is, int magic) throws IOException {
		mStream = is;
		mMagic = magic;
		byte[] hdr = new byte[8];
		mTotal = is.read(hdr);
		if (mTotal < 8) throw new IOException("End of stream reading header");
		int i;
		for (i = 0; i < 7; ++i) {
			if (hdr[i] != MAGIC[i])
				throw new IOException("Invalid RGSSAD");
		}
		mVersion = hdr[i];
	}
	private long read_dword(byte[] buf) throws IOException {
		if (mStream.read(buf) < 4) {
			return -1L;
		}
		mTotal += 4;
		int rv = mMagic ^ ((buf[0] & 0xFF) | ((buf[1] & 0xFF) << 8) | ((buf[2] & 0xFF) << 16) | ((buf[3] & 0xFF) << 24));
		mMagic = mMagic * 7 + 3;
		return rv & 0xFFFFFFFFL;
	}
	private byte read_byte() throws IOException {
		int c = mStream.read();
		if (c == -1) throw new IOException("Error parsing byte.");
		++mTotal;
		c ^= mMagic & 0xFF;
		mMagic = mMagic * 7 + 3;
		return (byte) c;
	}
	
	public synchronized boolean hasNext() throws IOException {
		return mStream.available() > available() + 4;
	}
	
	public synchronized void closeEntry() throws IOException {
		if (mEntry != null) {
			skip(-1);
			mTotal += mEntry.getSize();
			mMagic = (int) mEntry.getMagicKey();
			mEntry = null;
			mOffset = 0;
		}
	}
	public synchronized RgssEntry getNextEntry() throws IOException {
		byte[] buf = new byte[4];
		closeEntry();

		// Read filename
		long sz = read_dword(buf);
		if (sz == -1L) return null;
		byte[] fn = new byte[(int) sz];
		for (int i = 0; i < sz; ++i)
			fn[i] = read_byte();
		
		// Read filesize
		sz = read_dword(buf);

		// Return new entry
		mEntry = new RgssEntry(new String(fn, "UTF-8"), (int) sz, mMagic, mTotal);
		
		Log.e("CreateRPG", "ENTRY SZ : " + sz + ", NAME : " + mEntry.getName());
		
		return mEntry;
	}
	public synchronized int available() { return mEntry == null ? 0 : (int) (mEntry.getSize() - mOffset); }
	public void close() throws IOException { mStream.close(); mEntry = null; }
	public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
		if (mEntry == null) return 0;
		int b;
		if (mEntry.getSize() - mOffset < length)
			length = (int) (mEntry.getSize() - mOffset);
		length += offset;
		int k = offset;
		while (offset < length) {
			if ((b = mStream.read()) == -1) break;
			buffer[offset++] = (byte) (b ^ (0xFF & (mMagic >>> ((mOffset & 3) << 3))));
			if ((mOffset++ & 3) == 3) mMagic = mMagic * 7 + 3;
		}
		// n < offset < length
		return offset - k;
	}
	public synchronized int read(byte[] buffer) throws IOException { return read(buffer, 0, buffer.length); }
	public synchronized int read() throws IOException {
		int b;
		if (mEntry == null || mOffset >= mEntry.getSize() || (b = mStream.read()) == -1) return -1;
		b ^= 0xFF & (mMagic >>> ((mOffset & 3) << 3));
		if ((mOffset++ & 3) == 3) mMagic = mMagic * 7 + 3;
		return b;
	}
	public synchronized long skip(long byteCount) throws IOException {
		if (mEntry == null || mOffset >= mEntry.getSize()) return 0;
		if (byteCount == -1 || mOffset + byteCount > mEntry.getSize())
			byteCount = mEntry.getSize() - mOffset;
		long s = mStream.skip(byteCount);
		long k = ((mOffset % 4) + s) >> 2;
		while (k-- > 0) mMagic = mMagic * 7 + 3;
		mOffset += s;
		return s;
	}


}
