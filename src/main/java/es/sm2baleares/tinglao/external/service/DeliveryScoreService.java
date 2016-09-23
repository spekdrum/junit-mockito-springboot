package es.sm2baleares.tinglao.external.service;

/**
 * Servicio que simula un control de entregas para un sistema de control calidad.
 * Created by pablo.beltran on 21/09/2016.
 */
public interface DeliveryScoreService {

	/**
	 * Añade los puntos pasados por parámetro al global de puntos de la división por entregas.
	 * @param points puntos, los cuales pueden ser negativos.
	 */
	void submitDeliveryPoints(long points);

	/**
	 * Obtiene la puntuación actual del servicio de entregas, como medida simple de calidad.
	 * @return
	 */
	long getCurrentScore();
}
