package es.sm2baleares.tinglao.external.service;

import es.sm2baleares.tinglao.model.Order;

/**
 * Emula un servicio externo de envío de notificaciones por email.
 * Está mal diseñado a propósito, con la finalidad de buscar soluciones a la hora de testear una clase donde
 * está como dependencia.
 * Created by pablo.beltran on 21/09/2016.
 */
public class EmailService {

	private Order order;

	public EmailService(Order order) {
		this.order = order;
	}

	public void sendDeliveryNotification() {
		System.out.println("EmailService - ATENCIÓN! Enviando notificación producto " + order.getDescription());
	}

}
