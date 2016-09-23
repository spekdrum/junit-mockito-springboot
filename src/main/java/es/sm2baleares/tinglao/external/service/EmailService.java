package es.sm2baleares.tinglao.external.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sm2baleares.tinglao.model.Order;

/**
 * Emula un servicio externo de envío de notificaciones por email.
 * Está mal diseñado a propósito, con la finalidad de buscar soluciones a la hora de testear una clase donde
 * está como dependencia.
 * Created by pablo.beltran on 21/09/2016.
 */
public class EmailService {

	private Order order;

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	public EmailService(Order order) {
		this.order = order;
	}

	public void sendDeliveryNotification() {
		logger.info("EmailService - Enviando notificación producto " + order.getDescription());
	}

}
