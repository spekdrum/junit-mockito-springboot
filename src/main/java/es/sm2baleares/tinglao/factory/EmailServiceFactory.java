package es.sm2baleares.tinglao.factory;

import es.sm2baleares.tinglao.external.service.EmailService;
import es.sm2baleares.tinglao.model.Order;
import org.springframework.stereotype.Component;

/**
 * Factory para EmailService.
 * Created by pablo.beltran on 22/09/2016.
 */
@Component
public class EmailServiceFactory {

	/**
	 * Crea una instancia de EmailsService. El objetivo de este Factory es posibilitar el mocking de EmailService.
	 * @param order
	 * @return
	 */
	public EmailService buildEmailService(Order order) {
		return new EmailService(order);
	}

}
