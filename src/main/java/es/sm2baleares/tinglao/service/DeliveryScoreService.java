package es.sm2baleares.tinglao.service;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
public interface DeliveryScoreService {

	/**
	 * Añade los puntos pasados por parámetro al global de puntos de la división por entregas.
	 * @param points puntos, los cuales pueden ser negativos.
	 */
	void submitDeliveryPoints(long points);

	long getCurrentScore();
}
