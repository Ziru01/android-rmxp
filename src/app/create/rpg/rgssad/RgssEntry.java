package app.create.rpg.rgssad;

public class RgssEntry {
	String mFilename;
	int mFilesize, mMagic;
	long mOffset;
	
	public RgssEntry (String name, int size, int magic, long offset) {
		mFilename = name;
		mFilesize = size;
		mMagic = magic;
		mOffset = offset;
	}
	
	public String getName () {
		return mFilename;
	}
	
	public long getSize () {
		return mFilesize & 0xFFFFFFFFL;
	}
	
	public boolean isDirectory () {
		return mFilename.endsWith("\\");
	}

	public long getMagicKey () {
		return mMagic & 0xFFFFFFFFL;
	}
	
	public long getOffset () {
		return mOffset;
	}
	
}
