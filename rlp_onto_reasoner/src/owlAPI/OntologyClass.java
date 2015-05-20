package owlAPI;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OntologyClass {

	private String name;
	private String description;

	public void setName(String name) {
		this.name = name;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return description;
	}

	/* TODO: Get rid of magic numbers! */
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(name).append(description)
				.toHashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof OntologyClass))
			return false;
		if (obj == this)
			return true;
		OntologyClass rhs = (OntologyClass) obj;
		return new EqualsBuilder().append(name, rhs.name)
				.append(description, rhs.description).isEquals();
	}
}