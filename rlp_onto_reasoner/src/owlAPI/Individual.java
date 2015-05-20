package owlAPI;

import java.util.ArrayList;

public class Individual {
	private Integer FID;
	private String description;
	private ArrayList<String> DataPropertyNames;
	private ArrayList<Number> values;

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

	public void setValues(ArrayList<Number> rawValues) {
		this.values = rawValues;
	}

	public ArrayList<Number> getValues() {
		return values;
	}

	public void setDataPropertyNames(ArrayList<String> DataPropertyNames) {
		this.DataPropertyNames = DataPropertyNames;
	}
}