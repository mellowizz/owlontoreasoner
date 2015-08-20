package owlAPI;

import java.util.ArrayList;

public class Individual {
	private Integer FID;
	private String description;
	private ArrayList<String> DataPropertyNames;
	private ArrayList<Number> values;
	private ArrayList<String> stringValues;

	public void setFID(Integer number) {
		this.FID = number;
	}

	public Integer getFID() {
		return FID;
	}

	public ArrayList<String> getDataPropertyNames() {
		return DataPropertyNames;
	}

	public String getDescription() {
		return description;
	}

	public void setValueString(ArrayList<String> rawValues) {
		this.stringValues = rawValues;
	}
	
	public void setValues(ArrayList<Number> rawValues) {
		this.values = rawValues;
	}

	public ArrayList<Number> getValues() {
		return values;
	}

	public void setDataPropertyNames(ArrayList<String> DataPropertyNames) {
		this.DataPropertyNames = DataPropertyNames;
	}

	public ArrayList<String> getStringValues() {
		return stringValues;
	}
}