package es.sm2baleares.tinglao.service;

import es.sm2baleares.tinglao.exception.OrderAlreadyExistsException;
import es.sm2baleares.tinglao.exception.OrderException;
import es.sm2baleares.tinglao.model.Discount;
import es.sm2baleares.tinglao.model.Order;

import java.util.Date;

/**
 * Servicio para gestionar envíos de productos.
 * Created by pablo.beltran on 21/09/2016.
 */
public interface AmazonDeliveryService {

	//Days to estimated delivery
	public static final int ESTIMATED_DAYS_TO_DELIVER_PREMIUM = 2;
	public static final int ESTIMATED_DAYS_TO_DELIVER_REGULAR = 5;

	/**
	 * Crea un pedido nuevo.
	 *
	 * @param description descipción del pedido.
	 * @param basePrice precio inicial del pedido.
	 * @param premiumCustomer si el cliente es premium.
	 * @return
	 */
	Order initOrder(String description, double basePrice, boolean premiumCustomer) throws OrderAlreadyExistsException;

	/**
	 * Añade un descuento y recalcula el precio final.
	 *
	 * @param order Pedido al que se añade el descuento.
	 * @param discount Descuento que se añade
	 */
	void addDiscount(Order order, Discount discount);

	/**
	 * Marca un pedido como enviado.
	 * @param order
	 * @throws OrderException
	 */
	void markSent(Order order, Date sendDate) throws OrderException;

	/**
	 * Marca un pedido como entregado, enviando la puntuación calculada al servicio de puntuación.
	 * @param order
	 * @throws OrderException
	 */
	void markDelivered(Order order, Date deliverDate) throws OrderException;

}
