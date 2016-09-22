package es.sm2baleares.tinglao.external.service.impl;

import es.sm2baleares.tinglao.model.Order;
import es.sm2baleares.tinglao.external.service.OrderStorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by pablo.beltran on 22/09/2016.
 */
@Service
@Qualifier("OrderStorageService")
public class OrderStorageServiceImpl implements OrderStorageService {

	@Override
	public void store(Order order) {
		System.out.println("OrderStorageService - guardo pedido " + order.getDescription());
	}

	@Override
	public boolean exists(String description) {
		System.out.println("OrderStorageService - consulto si existe pedido " + description);
		return false;
	}
}
