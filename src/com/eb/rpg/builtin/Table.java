package com.eb.rpg.builtin;
import java.io.IOException;

import com.jinoh.ruby.marshal.CustomMarshallable;
import com.jinoh.ruby.marshal.Marshaler;
import com.jinoh.ruby.marshal.Unmarshaler;

public class Table implements CustomMarshallable {
	
	private short[][][] mArray;
	
	public Table () { }
	public Table (int xSize, int ySize, int zSize) {
		mArray = new short[zSize][ySize][xSize];
	}
	
	public short[][][] getInnerArray () {
		return mArray;
	}
	
	public void resize (int x, int y, int z) {
		short[][][] old = mArray;
		mArray = new short[x][y][z];
		if (x == 0 || y == 0 || z == 0 || old.length == 0 || old[0].length == 0 || old[0][0].length == 0)
			return;
		for (int i = 0; i < old.length; ++i)
			for (int j = 0; j < old[0].length; ++i)
				System.arraycopy(old[i][j], 0, mArray[i][j], 0, old[i][j].length);
	}
	
	public int getXSize () { return mArray.length > 0 && mArray[0].length > 0 ? mArray[0][0].length : 0; }
	public int getYSize () { return mArray.length > 0 ? mArray[0].length : 0; }
	public int getZSize () { return mArray.length; }
	
	public int getValueAt (int x, int y, int z) {
		return mArray[z][y][x] & 0xFFFF;
	}
	
	public void setValueAt (int x, int y, int z, int val) {
		mArray[z][y][x] = (short) val;
	}
	
	protected int unpackL(byte[] arr, int start) {
		return (arr[start] & 0xFF) | ((arr[start+1] & 0xFF) << 8) | ((arr[start+2] & 0xFF) << 16) | ((arr[start+3] & 0xFF) << 24);
	}
	
	protected void packL(byte[] arr, int start, int val) {
		arr[0] = (byte) val;
		arr[1] = (byte) (val >>> 8);
		arr[2] = (byte) (val >>> 16);
		arr[3] = (byte) (val >>> 24);
	}
	
	protected short unpackS(byte[] arr, int start) {
		return (short) ((arr[start] & 0xFF) | ((arr[start+1] & 0xFF) << 8));
	}
	
	protected void packS(byte[] arr, int start, short val) {
		arr[0] = (byte) val;
		arr[1] = (byte) (val >>> 8);
	}
	
	@Override
	public void load(Unmarshaler inst, byte[] data) throws IOException {
		unpackL(data, 0); // FIXME: This is 4byte integer (size), but what is the purpose of this?
		int sizeX = unpackL(data, 4),
				sizeY = unpackL(data, 8),
				sizeZ = unpackL(data, 12), p = 20;
		if (sizeX < 0 || sizeY < 0 || sizeZ < 0)
			throw new IOException ("Invalid size");
		mArray = new short[sizeZ][sizeY][sizeX];
		for (int i = 0; i < sizeZ; ++i) {
			for (int j = 0; j < sizeY; ++j) {
				for (int k = 0; k < sizeX; ++k) {
					mArray[i][j][k] = unpackS(data, p);
					p += 2;
				}
			}
		}
	}
	@Override
	public byte[] dump(Marshaler inst) throws IOException {
		int sizeZ = mArray.length,
				sizeY = mArray[0].length,
				sizeX = mArray[0][0].length, p = 20;
		byte[] data = new byte[p + (sizeZ * sizeY * sizeX * 2)];
		packL(data, 3, 0);
		packL(data, sizeX, 4);
		packL(data, sizeY, 8);
		packL(data, sizeZ, 12);
		packL(data, sizeX * sizeY * sizeZ, 16);
		for (int i = 0; i < sizeZ; ++i) {
			for (int j = 0; j < sizeY; ++j) {
				for (int k = 0; k < sizeX; ++k) {
					packS(data, p, mArray[i][j][k]);
					p += 2;
				}
			}
		}
		return data;
	}
	
}
