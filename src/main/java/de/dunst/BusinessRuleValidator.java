package de.dunst;

import java.util.List;
import java.util.function.Function;

public class BusinessRuleValidator {

    @FunctionalInterface
    public interface RuleHandler<T> {
        Function<T, Boolean> combineWith(Function<T, Boolean> next);
    }

    public static void main(String[] args) {
        // Example order to validate
        Order order = new Order(150.0, 5, "HIGH");

        // Define business rule handlers (in reverse order)
        List<RuleHandler<Order>> reversedHandlers = List.of(
                // Rule 3: Priority must be "HIGH" or "LOW"
                next -> o -> o.priority().matches("HIGH|LOW") && next.apply(o),
                // Rule 2: Item count must be <= 10
                next -> o -> o.itemCount() <= 10 && next.apply(o),
                // Rule 1: Amount must be > 100
                next -> o -> o.amount() > 100 && next.apply(o));

        // Terminal handler: Always returns true
        Function<Order, Boolean> terminal = _ -> true;

        // Build the chain
        Function<Order, Boolean> chain = reversedHandlers.stream()
                .reduce(
                        terminal,
                        (next, handler) -> handler.combineWith(next),
                        (_, _) -> {
                            throw new UnsupportedOperationException();
                        });

        // Execute validation
        boolean isValid = chain.apply(order);
        System.out.println("Order is valid: " + isValid); // Output: true
    }

    record Order(double amount, int itemCount, String priority) {
    }
}