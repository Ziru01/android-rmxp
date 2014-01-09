package com.jinoh.ruby.marshal;

import java.util.Map;

public class RubyIVar {

	Object mValue;
	Map<Symbol, Object> mIvars;

	public RubyIVar(Object value, Map<Symbol, Object> ivars) {
		mValue = value;
		mIvars = ivars;
	}
	
	public Object getValue() {
		return mValue;
	}

	public void setValue(Object value) {
		this.mValue = value;
	}

	public Map<Symbol, Object> getIvars() {
		return mIvars;
	}

	public void setIvars(Map<Symbol, Object> ivars) {
		this.mIvars = ivars;
	}
	
	public String toString () {
		if (mValue instanceof String)
			return mValue.toString();
		return "RubyIVar{value = " + mValue + ", ivars = " + mIvars + "}";
	}

}
