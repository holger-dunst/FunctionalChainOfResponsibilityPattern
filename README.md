# Chain of Responsibility Pattern in Java (Functional Approach)

## Pattern Overview
The **Chain of Responsibility** decouples request senders from receivers by allowing multiple handlers to process a request sequentially. In a functional paradigm:
- Handlers are standalone functions.
- Composition replaces object-based linking.
- Short-circuiting (via `Optional`) stops processing when a handler succeeds.

---

## Java Implementation
### Key Components:
1. **Handler Interface**:
   ```java
   @FunctionalInterface
   interface Handler {
       Optional<Response> handle(Request req);
       
       default Handler combineWith(Handler next) {
           return req -> this.handle(req).or(() -> next.handle(req));
       }
   }
   ```
2. **Concrete Handlers** (e.g., `AuthHandler`, `LoggerHandler`).

3. **Chain Composition**:
   ```java
   private static Handler buildChain(List<Handler> handlers) {
       Handler terminal = req -> Optional.empty();
       List<Handler> reversed = new ArrayList<>(handlers);
       Collections.reverse(reversed); // Fixes execution order
       
       return reversed.stream()
           .reduce(terminal, (next, handler) -> handler.combineWith(next));
   }
   ```

---

## Problem & Solution
### Issue:
- Initial code executed handlers **in reverse order** due to `reduce` composition logic.  
  Example: `BusinessLogicHandler` ran before `AuthHandler`.

### Fix:
- **Reverse the handler list** before reduction to ensure correct order:
  ```java
  List<Handler> reversedHandlers = new ArrayList<>(handlers);
  Collections.reverse(reversedHandlers);
  ```

### Why It Works:
| Step | Action | Result |
|------|--------|--------|
| 1    | Reverse list `[Logger, Auth, BusinessLogic]` → `[BusinessLogic, Auth, Logger]` | |
| 2    | `reduce` composes `Logger` → `Auth` → `BusinessLogic` | Execution order becomes `Logger` → `Auth` → `BusinessLogic`. |

---

## Example Outputs
### Case 1: User = "admin"
```java
Request request = new Request("admin", "data");
```
**Output**:
```
Logging request: data      // LoggerHandler
Response: Processed: data  // BusinessLogicHandler (Auth delegated)
```

### Case 2: User = "user"
```java
Request request = new Request("user", "data");
```
**Output**:
```
Logging request: data      // LoggerHandler
Response: Unauthorized!    // AuthHandler (short-circuited)
```

---

## Advantages
- **Decoupled Handlers**: No direct references between handlers.
- **Dynamic Composition**: Change order by modifying the list.
- **Immutability**: Stateless handlers avoid side effects.