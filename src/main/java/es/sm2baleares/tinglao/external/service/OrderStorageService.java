package es.sm2baleares.tinglao.external.service;

import es.sm2baleares.tinglao.model.Order;

/**
 * Simula un servicio externo para almacenar pedidos.
 * Created by pablo.beltran on 22/09/2016.
 */
public interface OrderStorageService {

	/**
	 * Almacena el pedido en el almacén de pedidos.
	 * @param order
	 */
	void store(Order order);

	/**
	 * Verifica que el pedido existe.
	 * @param description
	 * @return
	 */
	boolean exists(String description);
}
