package de.ipponsoft.services.rules;

import org.nakedobjects.applib.annotation.Named;
import org.nakedobjects.applib.annotation.Value;

@Value
@Named("Ergebnis")
public class Result {

	public StringBuffer value = new StringBuffer();
	public void setValue(String val) { value = new StringBuffer(val); }
	public String getValue() { return toString(); }
	public String toString() { return value.toString(); }
	
}
