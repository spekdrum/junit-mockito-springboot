package es.sm2baleares.tinglao.controller;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import es.sm2baleares.tinglao.exception.OrderAlreadyExistsException;
import es.sm2baleares.tinglao.exception.OrderException;
import es.sm2baleares.tinglao.external.service.DeliveryScoreService;
import es.sm2baleares.tinglao.model.Discount;
import es.sm2baleares.tinglao.model.Order;
import es.sm2baleares.tinglao.service.AmazonDeliveryService;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
@Controller
public class SampleController {

	@Inject
	private AmazonDeliveryService amazonDeliveryService;

	@Inject
	private DeliveryScoreService deliveryScoreService;

	@GetMapping("/")
	@ResponseBody
	public String home() throws OrderException, OrderAlreadyExistsException {

		final Date now = new Date();

		Order order = amazonDeliveryService.initOrder("Anal intruder", 90.25, true);
		amazonDeliveryService.addDiscount(order, new Discount("Promo fidelidad dilatada", 5.0));
		amazonDeliveryService.markSent(order, now);
		amazonDeliveryService.markDelivered(order, now);

		return "El servicio de entregas tiene " + deliveryScoreService.getCurrentScore() + " puntos";
	}

}
