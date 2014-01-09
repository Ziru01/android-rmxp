package app.create.rpg.rgssad;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class RgssOutputStream extends OutputStream {
	
	RgssEntry mEntry = null;
	long mOffset = 0, mTotal = 0;
	int mMagic;
	OutputStream mStream;
	public static byte[] MAGIC = new byte[] {0x52, 0x47, 0x53, 0x53, 0x41, 0x44, 0x00, 0x01};

	public RgssOutputStream(OutputStream os, int magic) throws IOException {
		mStream = os;
		mMagic = magic;
		os.write(MAGIC);
	}
	
	public synchronized void write_dword (int val) throws IOException {
		val ^= mMagic;
		mMagic = mMagic * 7 + 3;
		mStream.write(new byte[]{(byte) val, (byte) (val >>> 8), (byte) (val >>> 16), (byte) (val >>> 24)});
	}
	
	public synchronized void write_byte (byte val) throws IOException {
		val ^= (byte) mMagic;
		mMagic = mMagic * 7 + 3;
		mStream.write(val);
	}
	
	public synchronized void closeEntry() throws IOException {
		if (mEntry == null) return;
		if (mEntry.getOffset() != mOffset)
			throw new IOException ("Not fully written or overwritten");
		
		mMagic = (int) mEntry.getMagicKey();
		mEntry = null;
		mOffset = 0;
	}
	
	public synchronized void putNextEntry(RgssEntry entry) throws IOException {
		try {
			closeEntry();
			mMagic = entry.mMagic; // restore magic
			byte[] bytes = entry.getName().getBytes("UTF-8");
			write_dword(bytes.length);
			for (byte b : bytes)
				write_byte(b);
			
			write_dword((int) entry.getSize());
			entry.mMagic = mMagic;
			entry.mOffset = mTotal;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		closeEntry();
		mStream.close();
	}

	@Override
	public void flush() throws IOException {
		mStream.flush();
	}

	@Override
	public void write(int oneByte) throws IOException {
		if (mOffset + 1 >= mEntry.getSize())
			throw new IOException ("Out of size");
		mStream.write(oneByte ^ (mMagic >>> ((mOffset & 3) << 3)));
		if ((mOffset++ & 3) == 3)
			mMagic = mMagic * 7 + 3;
	}

}
