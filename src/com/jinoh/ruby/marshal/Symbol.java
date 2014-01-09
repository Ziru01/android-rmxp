package com.jinoh.ruby.marshal;

public class Symbol implements CharSequence, Cloneable {

	String mName;
	
	public Symbol(String name) {
		mName = name;
	}

	@Override
	public char charAt(int index) {
		return mName.charAt(index);
	}

	@Override
	public int length() {
		return mName.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return mName.subSequence(start, end);
	}
	
	@Override
	public String toString () {
		return mName;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Symbol(mName);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Symbol) && o.toString().equals(mName);
	}

	@Override
	public int hashCode() {
		return mName.hashCode();
	}

}
