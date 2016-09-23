package es.sm2baleares.tinglao.external.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import es.sm2baleares.tinglao.external.service.OrderStorageService;
import es.sm2baleares.tinglao.model.Order;

/**
 * Created by pablo.beltran on 22/09/2016.
 */
@Service
@Qualifier("OrderStorageService")
public class OrderStorageServiceImpl implements OrderStorageService {

	private static final Logger logger = LoggerFactory.getLogger(OrderStorageServiceImpl.class);

	@Override
	public void store(Order order) {
		logger.info("OrderStorageService - guardo pedido " + order.getDescription());
	}

	@Override
	public boolean exists(String description) {
		logger.info("OrderStorageService - consulto si existe pedido " + description);
		return false;
	}
}
