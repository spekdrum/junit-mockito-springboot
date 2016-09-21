package es.sm2baleares.tinglao.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import es.sm2baleares.tinglao.exception.OrderException;
import es.sm2baleares.tinglao.model.Discount;
import es.sm2baleares.tinglao.model.Order;
import es.sm2baleares.tinglao.service.impl.AmazonDeliveryServiceImpl;

//TODO: caso spy

/**
 * Created by pablo.beltran on 21/09/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class AmazonDeliveryServiceImplTest {

	private static final double EPSILON_ALLOWED_DOUBLE_EQUALS = 0.0;
	public static final String TEST_PRODUCT = "Test";
	public static final int DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
	public static final int HOURS_A_DAY = 24;
	private static final Date JUST_NOW = new Date();

	@Mock
	private DeliveryScoreService deliveryScoreService;

	@InjectMocks
	private AmazonDeliveryServiceImpl amazonDeliveryService;


	@Test
	public void newOrderShouldReturnInitializedOrder() throws Exception {

		//Given
		final String desc = TEST_PRODUCT;
		final double price = 5.0;
		final boolean premium = true;

		//When
		Order order = amazonDeliveryService.newOrder(desc, price, premium);

		//Then
		assertEquals(order.getDescription(), desc);
		assertEquals(order.getBasePrice(), price, EPSILON_ALLOWED_DOUBLE_EQUALS);
		assertTrue(order.isPremium());

		//Comprobaciones
		assertEquals("El precio final debe ser igual al precio base al crear un pedido",
				order.getFinalPrice(), order.getBasePrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
		assertFalse("Los pedidos nuevos no pueden estar enviados", order.isSent());
		assertFalse("Los pedidos nuevos no pueden estar entregados", order.isDelivered());
	}

	@Test
	public void addDiscountShouldCalcFinalPriceWhenDiscountsAdded() throws Exception {
		//Given
		final double basePrice = 5.0;
		final double discount = 10.0;
		final double finalPrice = basePrice - (basePrice * discount / 100.0);

		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, basePrice, true);

		//When
		amazonDeliveryService.addDiscount(order, new Discount("Fidelidad", discount));

		//Then
		assertEquals(finalPrice, order.getFinalPrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
	}

	@Test
	public void addDiscountShouldAcumulateDiscountsWhenDiscountsAdded() throws Exception {
		//Given
		final double basePrice = 150.0;
		final double discount1 = 10.0;
		final double discount2 = 5.0;
		final double finalPrice = basePrice - (basePrice * (discount1 + discount2) / 100.0);

		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, basePrice, true);

		//When
		amazonDeliveryService.addDiscount(order, new Discount("Fidelidad", discount1));
		amazonDeliveryService.addDiscount(order, new Discount("Especial", discount2));

		//Then
		assertEquals(finalPrice, order.getFinalPrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
	}

	@Test
	public void markSentShouldEstimateDeliveryDate() throws OrderException {
		//Given
		Order premiumOrder = amazonDeliveryService.newOrder(TEST_PRODUCT, 150.0, true);
		Order regularOrder = amazonDeliveryService.newOrder(TEST_PRODUCT, 150.0, false);

		//When
		amazonDeliveryService.markSent(premiumOrder, JUST_NOW);
		amazonDeliveryService.markSent(regularOrder, JUST_NOW);


		//Then
		//Calcular la diferencia de días entre fecha estimada de entrega y fecha de envío.
		long daysDiffPremium = (premiumOrder.getEstimatedDelivery().getTime() - premiumOrder.getSendDate().getTime())
				/ DAY_MILLISECONDS;

		long daysDiffRegular = (regularOrder.getEstimatedDelivery().getTime() - regularOrder.getSendDate().getTime())
				/ DAY_MILLISECONDS;

		assertEquals("La fecha de entrega estimada para clientes premium debería ser de " +
				AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_PREMIUM + " después de la fecha de envío.",
				AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_PREMIUM, daysDiffPremium);

		assertEquals("La fecha de entrega estimada para clientes regulares debería ser de " +
						AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_REGULAR + " después de la fecha de envío.",
				AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_REGULAR, daysDiffRegular);
	}

	@Test (expected = OrderException.class)
	public void markSentShouldThrowOrderExceptionWhenOrderAlreadySent() throws OrderException {
		//Given
		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, true);

		//When
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markSent(order, JUST_NOW);

		//Then throw OrderException
	}


	@Test (expected = OrderException.class)
	public void markDeliveredShouldThrowOrderExceptionWhenOrderIsNotSent() throws OrderException {
		//Given
		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, true);

		//When
		amazonDeliveryService.markDelivered(order, JUST_NOW);

		//Then throw OrderException
	}

	@Test
	public void markDeliveredShouldSendRightScoreWhenRegularOrder() throws OrderException {
		final long expectedPointsRegular = AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_REGULAR * HOURS_A_DAY;

		//Given
		Order regularOrder = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, false);

		//When
		amazonDeliveryService.markSent(regularOrder, JUST_NOW);
		amazonDeliveryService.markDelivered(regularOrder, JUST_NOW);

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertEquals("Los puntos recibidos siendo pedido regular deben ser " + expectedPointsRegular,
				expectedPointsRegular, argumentCaptor.getValue().longValue());
	}

	@Test
	public void markDeliveredShouldSendRightScoreWhenPremiumOrder() throws OrderException {
		final long expectedPointsPremium = AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_PREMIUM * HOURS_A_DAY;

		//Given
		Order premiumOrder = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, true);

		//When
		amazonDeliveryService.markSent(premiumOrder, JUST_NOW);
		amazonDeliveryService.markDelivered(premiumOrder, JUST_NOW);

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertEquals("Los puntos recibidos siendo pedido premium deben ser " + expectedPointsPremium,
				expectedPointsPremium, argumentCaptor.getValue().longValue());
	}

	@Test
	public void markDeliveredShouldSubmitNegativeScoreWhenOrderDeliversAfterEstimatedDate() throws OrderException {
		//Given
		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, true);

		//When / el pedido se entrega pasado un día de fecha prevista
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order,
				new Date(order.getEstimatedDelivery().getTime() + DAY_MILLISECONDS));

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertTrue("La puntuación debe ser menor que 0 cuando la fecha de entrega es posterior a la estimada",
				argumentCaptor.getValue() < 0);

	}

	@Test (expected = OrderException.class)
	public void markDeliveredShouldThrowOrderExceptionWhenAlreadyDelivered() throws OrderException {
		//Given
		Order order = amazonDeliveryService.newOrder(TEST_PRODUCT, 10.0, true);

		//When
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order, JUST_NOW);

		//Then throw OrderException
	}

//	private Date tomorrow() {
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(new Date());
//		calendar.add(Calendar.DAY_OF_MONTH, 1);
//		return calendar.getTime();
//	}
}