package es.sm2baleares.tinglao.model;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
public class Discount {
	public String description;
	public double percent;

	public Discount(String description, double percent) {
		this.percent = percent;
		this.description = description;
	}

	/**
	 * Getter for property 'description'.
	 *
	 * @return Value for property 'description'.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter for property 'description'.
	 *
	 * @param description Value to set for property 'description'.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Getter for property 'percent'.
	 *
	 * @return Value for property 'percent'.
	 */
	public double getPercent() {
		return percent;
	}

	/**
	 * Setter for property 'percent'.
	 *
	 * @param percent Value to set for property 'percent'.
	 */
	public void setPercent(double percent) {
		this.percent = percent;
	}
}
