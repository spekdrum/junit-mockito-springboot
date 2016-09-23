package es.sm2baleares.tinglao.service.impl;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import es.sm2baleares.tinglao.exception.OrderAlreadyExistsException;
import es.sm2baleares.tinglao.exception.OrderException;
import es.sm2baleares.tinglao.external.service.DeliveryScoreService;
import es.sm2baleares.tinglao.external.service.EmailService;
import es.sm2baleares.tinglao.external.service.OrderStorageService;
import es.sm2baleares.tinglao.factory.EmailServiceFactory;
import es.sm2baleares.tinglao.model.Discount;
import es.sm2baleares.tinglao.model.Order;
import es.sm2baleares.tinglao.service.AmazonDeliveryService;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
@Service
@Qualifier("AmazonDeliveryService")
public class AmazonDeliveryServiceImpl implements AmazonDeliveryService {

	@Inject
	private DeliveryScoreService deliveryScoreService;

	@Inject
	private OrderStorageService orderStorageService;

	@Inject
	private EmailServiceFactory emailServiceFactory;

	/**
	 * {@inheritDoc}
	 */
	public Order initOrder(String description, double basePrice, boolean premiumCustomer)
			throws OrderAlreadyExistsException {

		if (orderStorageService.exists(description)) {
			throw new OrderAlreadyExistsException();
		}

		Order order = new Order();
		order.setDescription(description);
		order.setBasePrice(basePrice);
		order.setDelivered(false);
		order.setSent(false);
		order.setPremium(premiumCustomer);
		calcFinalPrice(order);

		orderStorageService.store(order);
		return order;
	}


	/**
	 * {@inheritDoc}
	 */
	public void addDiscount(Order order, Discount discount) {
		if (discount != null) {
			order.getDiscounts().add(discount);
		}
		calcFinalPrice(order);

		orderStorageService.store(order);
	}

	/**
	 * {@inheritDoc}
	 */
	public void markSent(Order order, Date sendDate) throws OrderException {
		if (order.isSent()) {
			throw new OrderException("El pedido ya está enviado");
		}

		order.setSendDate(sendDate);
		Date estimatedDelivery = calcEstimatedDeliveryDate(order.isPremium());

		order.setEstimatedDelivery(estimatedDelivery);
		order.setSent(true);

		orderStorageService.store(order);
	}

	/**
	 * {@inheritDoc}
	 */
	public void markDelivered(Order order, Date deliverDate) throws OrderException {
		if (!order.isSent()) {
			throw new OrderException("El pedido no está enviado");
		}

		if (order.isDelivered()) {
			throw new OrderException("El pedido ya está entregado");
		}

		order.setDelivered(true);
		order.setRealDelivery(deliverDate);

		orderStorageService.store(order);

		long deliveryScore = calcDeliveryDateScore(order);

		//Submit score
		deliveryScoreService.submitDeliveryPoints(deliveryScore);

		//Send email notification
		EmailService emailService = emailServiceFactory.buildEmailService(order);

		emailService.sendDeliveryNotification();
	}

	/**
	 * Dado un pedido entregado, calcula la puntuación obtenida por la diferencia entre fecha estimada y de entrega.
	 * @param order
	 * @return
	 */
	private long calcDeliveryDateScore(Order order) {
		final long diff = order.getEstimatedDelivery().getTime() - order.getRealDelivery().getTime();
		return diff / (60 * 60 * 1000);
	}

	/**
	 * Calcula el precio final del producto, teniendo en cuenta los descuentos.
	 * @param order
	 */
	private void calcFinalPrice(Order order) {
		double totalDiscount = 0;

		for (Discount discount : order.getDiscounts()) {
			totalDiscount += discount.getPercent();
		}

		//calc discount
		double finalPrice = order.getBasePrice() - (order.getBasePrice() * totalDiscount / 100.0);

		//round 2 decimals chapucer
		finalPrice *= 100;
		finalPrice = Math.round(finalPrice);
		finalPrice /= 100;

		//set final price
		order.setFinalPrice(finalPrice);
	}

	/**
	 * Calcula fecha estimada de entrega.
	 * @param premiumCustomer
	 * @return
	 */
	private Date calcEstimatedDeliveryDate(boolean premiumCustomer) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		if (premiumCustomer) {
			calendar.add(Calendar.DAY_OF_MONTH, ESTIMATED_DAYS_TO_DELIVER_PREMIUM);
		} else {
			calendar.add(Calendar.DAY_OF_MONTH, ESTIMATED_DAYS_TO_DELIVER_REGULAR);
		}
		return calendar.getTime();
	}
}
