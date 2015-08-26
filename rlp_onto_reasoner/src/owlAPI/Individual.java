package owlAPI;

import java.util.ArrayList;
import java.util.HashMap;

public class Individual {
	private Integer FID;
	private String description;
	private HashMap<String, Number> values;
	private HashMap<String, String> stringValues;
	//private ArrayList<String> DataPropertyNames;
	//private ArrayList<Number> values;
	//private ArrayList<String> stringValues;

	public void setFID(Integer number) {
		this.FID = number;
	}

	public Integer getFID() {
		return FID;
	}

	//public ArrayList<String> getDataPropertyNames() {
	//	return DataPropertyNames;
	//}

	public String getDescription() {
		return description;
	}

	public void addValueString(String valueName, String value) {
		this.stringValues.put(valueName, value);
	}
	
	public void addValues(String valueName, Number value) {
		this.values.put(valueName, value);
	}
	
	public void setValueString(HashMap<String, String> rawValues){
		this.stringValues = rawValues;
	}
	
	public void setValues(HashMap<String, Number> rawValues){
		this.values = rawValues;
	}

	public HashMap<String, Number> getValues() {
		return values;
	}
	/*
	public void setDataPropertyNames(ArrayList<String> DataPropertyNames) {
		this.DataPropertyNames = DataPropertyNames;
	}*/

	public HashMap<String, String> getStringValues() {
		return stringValues;
	}
}