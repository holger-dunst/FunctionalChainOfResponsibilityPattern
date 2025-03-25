package de.dunst;

import java.util.*;

public class FunctionalChainOfResponsibility {

    public static void main(String[] args) {
        // Define handlers (as functions)
        List<Handler> handlers = Arrays.asList(
                new LoggerHandler(),
                new AuthHandler(),
                new BusinessLogicHandler()
        );

        // Build the chain by composing handlers in reverse order
        Handler chain = buildChain(handlers);

        // Test the chain
        Request request = new Request("admin", "data");
        Optional<Response> response = chain.handle(request);

        response.ifPresentOrElse(
                r -> System.out.println("Response: " + r.message()),
                () -> System.out.println("Request was not handled")
        );
    }

    // Helper to build the chain using reduce
    private static Handler buildChain(List<Handler> handlers) {
        // Terminal handler (end of chain)
        Handler terminal = req -> Optional.empty();

        // Reverse the list to ensure correct order: first handler -> last handler -> terminal
        List<Handler> reversedHandlers = new ArrayList<>(handlers);
        Collections.reverse(reversedHandlers); // Fixes execution order

        return reversedHandlers.stream()
                .reduce(
                        terminal,
                        (next, handler) -> handler.combineWith(next),
                        (a, b) -> { throw new UnsupportedOperationException(); }
                );
    }

    // --- Handler Definitions ---

    @FunctionalInterface
    interface Handler {
        Optional<Response> handle(Request req);

        // Renamed to avoid clash with Function's andThen
        default Handler combineWith(Handler next) {
            return req -> this.handle(req).or(() -> next.handle(req));
        }
    }

    static class LoggerHandler implements Handler {
        @Override
        public Optional<Response> handle(Request req) {
            System.out.println("Logging request: " + req.data());
            return Optional.empty(); // Always delegate to next
        }
    }

    static class AuthHandler implements Handler {
        @Override
        public Optional<Response> handle(Request req) {
            if (!req.user().equals("admin")) {
                return Optional.of(new Response("Unauthorized!"));
            }
            return Optional.empty(); // Delegate if authorized
        }
    }

    static class BusinessLogicHandler implements Handler {
        @Override
        public Optional<Response> handle(Request req) {
            return Optional.of(new Response("Processed: " + req.data()));
        }
    }

    // --- Data Classes ---
    record Request(String user, String data) {
    }

    record Response(String message) {
    }
}