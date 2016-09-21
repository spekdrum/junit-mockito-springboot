package es.sm2baleares.tinglao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
public class Order {

	private String description;
	private double basePrice;
	private List<Discount> discounts;
	private double finalPrice;

	private boolean sent;
	private Date sendDate;
	private Date estimatedDelivery;
	private boolean delivered;
	private Date realDelivery;
	private boolean premium;

	public Order() {
		discounts = new ArrayList<>();
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
	 * Getter for property 'basePrice'.
	 *
	 * @return Value for property 'basePrice'.
	 */
	public double getBasePrice() {
		return basePrice;
	}

	/**
	 * Setter for property 'basePrice'.
	 *
	 * @param basePrice Value to set for property 'basePrice'.
	 */
	public void setBasePrice(double basePrice) {
		this.basePrice = basePrice;
	}

	/**
	 * Getter for property 'discounts'.
	 *
	 * @return Value for property 'discounts'.
	 */
	public List<Discount> getDiscounts() {
		return discounts;
	}

	/**
	 * Setter for property 'discounts'.
	 *
	 * @param discounts Value to set for property 'discounts'.
	 */
	public void setDiscounts(List<Discount> discounts) {
		this.discounts = discounts;
	}

	/**
	 * Getter for property 'finalPrice'.
	 *
	 * @return Value for property 'finalPrice'.
	 */
	public double getFinalPrice() {
		return finalPrice;
	}

	/**
	 * Setter for property 'finalPrice'.
	 *
	 * @param finalPrice Value to set for property 'finalPrice'.
	 */
	public void setFinalPrice(double finalPrice) {
		this.finalPrice = finalPrice;
	}

	/**
	 * Getter for property 'sent'.
	 *
	 * @return Value for property 'sent'.
	 */
	public boolean isSent() {
		return sent;
	}

	/**
	 * Setter for property 'sent'.
	 *
	 * @param sent Value to set for property 'sent'.
	 */
	public void setSent(boolean sent) {
		this.sent = sent;
	}

	/**
	 * Getter for property 'sendDate'.
	 *
	 * @return Value for property 'sendDate'.
	 */
	public Date getSendDate() {
		return sendDate;
	}

	/**
	 * Setter for property 'sendDate'.
	 *
	 * @param sendDate Value to set for property 'sendDate'.
	 */
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	/**
	 * Getter for property 'estimatedDelivery'.
	 *
	 * @return Value for property 'estimatedDelivery'.
	 */
	public Date getEstimatedDelivery() {
		return estimatedDelivery;
	}

	/**
	 * Setter for property 'estimatedDelivery'.
	 *
	 * @param estimatedDelivery Value to set for property 'estimatedDelivery'.
	 */
	public void setEstimatedDelivery(Date estimatedDelivery) {
		this.estimatedDelivery = estimatedDelivery;
	}

	/**
	 * Getter for property 'delivered'.
	 *
	 * @return Value for property 'delivered'.
	 */
	public boolean isDelivered() {
		return delivered;
	}

	/**
	 * Setter for property 'delivered'.
	 *
	 * @param delivered Value to set for property 'delivered'.
	 */
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

	/**
	 * Getter for property 'realDelivery'.
	 *
	 * @return Value for property 'realDelivery'.
	 */
	public Date getRealDelivery() {
		return realDelivery;
	}

	/**
	 * Setter for property 'realDelivery'.
	 *
	 * @param realDelivery Value to set for property 'realDelivery'.
	 */
	public void setRealDelivery(Date realDelivery) {
		this.realDelivery = realDelivery;
	}

	/**
	 * Getter for property 'premium'.
	 *
	 * @return Value for property 'premium'.
	 */
	public boolean isPremium() {
		return premium;
	}

	/**
	 * Setter for property 'premium'.
	 *
	 * @param premium Value to set for property 'premium'.
	 */
	public void setPremium(boolean premium) {
		this.premium = premium;
	}
}
