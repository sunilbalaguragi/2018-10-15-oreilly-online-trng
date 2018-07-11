package com.mattstine.dddworkshop.pizzashop.ordering;

import com.mattstine.dddworkshop.pizzashop.infrastructure.Amount;
import com.mattstine.dddworkshop.pizzashop.infrastructure.EventLog;
import com.mattstine.dddworkshop.pizzashop.payments.PaymentRef;
import lombok.*;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Stine
 */
@Value
public class Order {
	OrderType type;
	EventLog eventLog;
	OrderRef id;
	@NonFinal
	OrderState state;
	List<Pizza> pizzas;
	@NonFinal
	@Setter
	PaymentRef paymentRef;

	@Builder
	private Order(@NonNull OrderType type, @NonNull EventLog eventLog, @NonNull OrderRef ref) {
		this.type = type;
		this.eventLog = eventLog;
		this.id = ref;
		this.pizzas = new ArrayList<>();
	}

	public boolean isPickupOrder() {
		return this.type == OrderType.PICKUP;
	}

	public boolean isDeliveryOrder() {
		return this.type == OrderType.DELIVERY;
	}

	public boolean isSubmitted() {
		return this.state == OrderState.SUBMITTED;
	}

	public void addPizza(Pizza pizza) {
		this.pizzas.add(pizza);
		eventLog.publish(new PizzaAddedEvent(id, pizza));
	}

	public void submit() {
		if (this.pizzas.isEmpty()) {
			throw new IllegalStateException("Cannot submit Order without at least one Pizza");
		}

		this.state = OrderState.SUBMITTED;
		eventLog.publish(new OrderPlacedEvent());
	}

	public Amount calculatePrice() {
		return this.pizzas.stream()
				.map(Pizza::getPrice)
				.reduce(Amount.of(0,0), Amount::plus);
	}

	public boolean isPaid() {
		return state == OrderState.PAID;
	}

	public void markPaid() {
		this.state = OrderState.PAID;
	}

}
