package es.sm2baleares.tinglao.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import es.sm2baleares.tinglao.exception.OrderAlreadyExistsException;
import es.sm2baleares.tinglao.exception.OrderException;
import es.sm2baleares.tinglao.external.service.DeliveryScoreService;
import es.sm2baleares.tinglao.external.service.EmailService;
import es.sm2baleares.tinglao.external.service.OrderStorageService;
import es.sm2baleares.tinglao.factory.EmailServiceFactory;
import es.sm2baleares.tinglao.model.Discount;
import es.sm2baleares.tinglao.model.Order;
import es.sm2baleares.tinglao.service.AmazonDeliveryService;

/**
 * Created by pablo.beltran on 21/09/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class AmazonDeliveryServiceImplTest {

	private static final double EPSILON_ALLOWED_DOUBLE_EQUALS = 0.0;
	private static final String TEST_PRODUCT = "Test";
	private static final double TEST_PRODUCT_PRICE = 150.0;
	private static final int DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
	private static final int HOURS_A_DAY = 24;
	private static final Date JUST_NOW = new Date();

	@Mock
	private DeliveryScoreService deliveryScoreService;

	@Mock
	private EmailService emailService;

	@Mock
	private OrderStorageService orderStorageService;

	@Mock
	private EmailServiceFactory emailServiceFactory;

	@InjectMocks
	private AmazonDeliveryServiceImpl amazonDeliveryService;

	@Captor
	private ArgumentCaptor<Long> argumentCaptor;

	private Set<String> ordersBag;

	@Before
	public void setUp() throws Exception {
		Mockito.doNothing().when(deliveryScoreService).submitDeliveryPoints(Mockito.anyLong());

		Mockito.when(emailServiceFactory.buildEmailService(Mockito.any(Order.class))).thenReturn(emailService);
		Mockito.doNothing().when(emailService).sendDeliveryNotification();

		//Mockeo de orderStorageService con control de pedidos duplicados
		ordersBag = new HashSet<>();

		//Mock de operación orderStorageService.store
		Mockito.doAnswer(invocationOnMock -> {
			Order order = (Order)invocationOnMock.getArguments()[0];
			ordersBag.add(order.getDescription());
			return null;
		}).when(orderStorageService).store(Mockito.any(Order.class));

		//Mock de operación orderStorageService.exists
		Mockito.doAnswer(invocationOnMock -> {
			String desc = (String)invocationOnMock.getArguments()[0];
			return ordersBag.contains(desc);
		}).when(orderStorageService).exists(Mockito.anyString());
	}

	@Test
	public void initOrderShouldReturnInitializedOrder() throws OrderAlreadyExistsException {

		//Given
		Order testOrder = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		Order order = amazonDeliveryService.initOrder(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//Then
		assertEquals(testOrder.getDescription(), order.getDescription());
		assertEquals(testOrder.getBasePrice(), order.getBasePrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
		assertTrue(testOrder.isPremium() == order.isPremium());

		assertEquals("El precio final debe ser igual al precio base al crear un pedido",
				order.getFinalPrice(), order.getBasePrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
		assertFalse("Los pedidos nuevos no pueden estar enviados", order.isSent());
		assertFalse("Los pedidos nuevos no pueden estar entregados", order.isDelivered());
	}

	@Test
	public void initOrderShouldAllowManyOrders() throws OrderAlreadyExistsException {
		//Given - when
		Order testOrder1 = amazonDeliveryService.initOrder(TEST_PRODUCT + "1", TEST_PRODUCT_PRICE, true);
		Order testOrder2 = amazonDeliveryService.initOrder(TEST_PRODUCT + "2", TEST_PRODUCT_PRICE, true);

		//Then - No hay error alguno. No hay Asserts, pero podemos hacer un Verify
		Mockito.verify(orderStorageService, Mockito.times(2)).exists(Mockito.anyString());
	}

	@Test (expected = OrderAlreadyExistsException.class)
	public void initOrderShouldThrowOrderAlreadyExistsExceptionWhenInitDuplicatedOrders()
			throws OrderAlreadyExistsException {
		//Given - when
		Order testOrder1 = amazonDeliveryService.initOrder(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);
		Order testOrder2 = amazonDeliveryService.initOrder(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//Then - throw new OrderAlreadyExistsException
	}

	@Test
	public void addDiscountShouldCalcFinalPriceWhenDiscountsAdded() {
		//Given
		final double discount = 10.0;
		final double finalPrice = TEST_PRODUCT_PRICE - (TEST_PRODUCT_PRICE * discount / 100.0);
		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.addDiscount(order, new Discount("Fidelidad", discount));

		//Then
		assertEquals(finalPrice, order.getFinalPrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
	}

	@Test
	public void addDiscountShouldAcumulateDiscountsWhenDiscountsAdded() {
		//Given
		final double discount1 = 10.0;
		final double discount2 = 5.0;
		final double finalPrice = TEST_PRODUCT_PRICE - (TEST_PRODUCT_PRICE * (discount1 + discount2) / 100.0);

		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.addDiscount(order, new Discount("Fidelidad", discount1));
		amazonDeliveryService.addDiscount(order, new Discount("Especial", discount2));

		//Then
		assertEquals(finalPrice, order.getFinalPrice(), EPSILON_ALLOWED_DOUBLE_EQUALS);
	}

	@Test
	public void markSentShouldEstimateDeliveryDate() throws OrderException {
		//Given
		Order premiumOrder = buildOrderTestObject(TEST_PRODUCT + "P", TEST_PRODUCT_PRICE, true);
		Order regularOrder = buildOrderTestObject(TEST_PRODUCT + "R", TEST_PRODUCT_PRICE, false);

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
		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markSent(order, JUST_NOW);

		//Then throw OrderException
	}


	@Test (expected = OrderException.class)
	public void markDeliveredShouldThrowOrderExceptionWhenOrderIsNotSent() throws OrderException {
		//Given
		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.markDelivered(order, JUST_NOW);

		//Then throw OrderException
	}

	@Test
	public void markDeliveredShouldSendRightScoreWhenRegularOrder() throws OrderException {
		final long expectedPointsRegular = AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_REGULAR * HOURS_A_DAY;

		//Given
		Order regularOrder = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, false);

		//When
		amazonDeliveryService.markSent(regularOrder, JUST_NOW);
		amazonDeliveryService.markDelivered(regularOrder, JUST_NOW);

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		//ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertEquals("Los puntos recibidos siendo pedido regular deben ser " + expectedPointsRegular,
				expectedPointsRegular, argumentCaptor.getValue().longValue());
	}

	@Test
	public void markDeliveredShouldSendRightScoreWhenPremiumOrder() throws OrderException {
		final long expectedPointsPremium = AmazonDeliveryService.ESTIMATED_DAYS_TO_DELIVER_PREMIUM * HOURS_A_DAY;

		//Given
		Order premiumOrder = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.markSent(premiumOrder, JUST_NOW);
		amazonDeliveryService.markDelivered(premiumOrder, JUST_NOW);

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		//ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertEquals("Los puntos recibidos siendo pedido premium deben ser " + expectedPointsPremium,
				expectedPointsPremium, argumentCaptor.getValue().longValue());
	}

	@Test
	public void markDeliveredShouldSubmitNegativeScoreWhenOrderDeliversAfterEstimatedDate() throws OrderException {
		//Given
		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When / el pedido se entrega pasado un día de fecha prevista
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order,
				new Date(order.getEstimatedDelivery().getTime() + DAY_MILLISECONDS));

		//Observamos con qué valor se ha invocado deliveryScoreService.submitDeliveryPoints
		//ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		Mockito.verify(deliveryScoreService).submitDeliveryPoints(argumentCaptor.capture());

		//Then
		assertTrue("La puntuación debe ser menor que 0 cuando la fecha de entrega es posterior a la estimada",
				argumentCaptor.getValue() < 0);

	}

	@Test (expected = OrderException.class)
	public void markDeliveredShouldThrowOrderExceptionWhenAlreadyDelivered() throws OrderException {
		//Given
		Order order = buildOrderTestObject(TEST_PRODUCT, TEST_PRODUCT_PRICE, true);

		//When
		amazonDeliveryService.markSent(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order, JUST_NOW);
		amazonDeliveryService.markDelivered(order, JUST_NOW);

		//Then throw OrderException
	}

	private Order buildOrderTestObject(String description, double basePrice, boolean premiumCustomer) {
		Order order = new Order();
		order.setDescription(description);
		order.setBasePrice(basePrice);
		order.setPremium(premiumCustomer);
		return order;
	}
}