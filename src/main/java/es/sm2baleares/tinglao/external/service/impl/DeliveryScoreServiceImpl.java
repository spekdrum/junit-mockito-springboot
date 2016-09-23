package es.sm2baleares.tinglao.external.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import es.sm2baleares.tinglao.external.service.DeliveryScoreService;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
@Service
@Qualifier("DeliveryScoreService")
public class DeliveryScoreServiceImpl implements DeliveryScoreService {

	private long totalPoints;

	private static final Logger logger = LoggerFactory.getLogger(DeliveryScoreServiceImpl.class);

	public void submitDeliveryPoints(long points) {
		logger.info("DeliveryScoreService - SUMO " + points + " PUNTOS!");
		totalPoints += points;
	}

	public long getCurrentScore() {
		return totalPoints;
	}
}
