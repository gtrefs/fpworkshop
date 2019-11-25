package eu.javaland.fpworkshop.purity;


import eu.javaland.fpworkshop.purity.CafeExample.Charge;
import eu.javaland.fpworkshop.purity.CafeExample.CreditCard;
import io.vavr.API;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;

import static io.vavr.API.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Exercises {

    // Exercise 1
    // Is expression add(one, two) pure?
    // Describe why and/or why not.

    // add is pure, everything what the function does is expressed by the returned value
    @Test
    public void addition() {
        var one = 1;
        var two = 2;

        var three = add(one, two);

        assertThat(three, equalTo(3));
    }

    private int add(int a, int b) {
        return a + b;
    }

    // Exercise 2
    // Is expression divide(one, zero) pure?
    // Describe why and/or why not.

    // division is not pure, the ArithmeticException is not represented in the returned value.
    @Test
    public void division(){
        var zero = 0;
        var one = 1;

        var dividedByZero = divide(one, zero);

        assertThat(dividedByZero, equalTo(Integer.MAX_VALUE));
    }

    private int divide(int a, int b) {
        return a / b;
    }

    // Exercise 3
    // Do you know a way how to make divide a pure function?
    // Hint: Think about using a type representing a try of a division.

    private Try<Integer> divide2(int a, int b) {
         return Try.of(() -> divide(a, b));
    }

    // Exercise 4
    // Is expression checkout(charges) a pure function?
    // Describe why and/or why not.

    // No, the function is not pure. It returns void. The only way this function could do something is by side effects.
    @Test
    public void checkoutThreeCoffees(){
        var creditCard = new CreditCard();
        var charges = List.fill(3, () -> Charge.of(creditCard, new BigDecimal(1)));

        checkout(charges);
    }

    private void checkout(List<Charge> charges) {
        var chargeFromPaymentProvider = (Consumer<Charge>) charge -> {
            throw new IllegalStateException("Could not reach server");
        };

        charges.forEach(chargeFromPaymentProvider);
    }

    // Exercise 5
    // Make function checkout2(charges, paymentProvider) pure
    // Hint: Use type Future to represent charges in the future
    // Hint 2: Use function Charge#combine
    @Test
    public void checkoutFailsInTheFuture(){
        var creditCard = new CreditCard();
        var charges = List.fill(3, Charge.of(creditCard, new BigDecimal(1)));
        var paymentProvider = API.<Charge, Clearance>Function(charge -> Clearance.uncleared(charge, "Could not reach server"));

        var clearance = checkout2(charges, paymentProvider);

        assertThat(clearance.get(), equalTo(Clearance.uncleared(Charge.of(creditCard, new BigDecimal(3)), "Could not reach server")));
    }

    @Test
    public void checkoutSucceedsWithADifferentPaymentProvider(){
        var creditCard = new CreditCard();
        var charges = List.fill(3, Charge.of(creditCard, new BigDecimal(1)));
        var paymentProvider = API.<Charge, Clearance>Function(charge -> Clearance.cleared(charge.price));

        var clearance = checkout2(charges, paymentProvider);

        assertThat(clearance.get(), equalTo(Clearance.cleared(new BigDecimal(3))));
    }

    private Future<Clearance> checkout2(List<Charge> charges, Function1<Charge, Clearance> paymentProvider){
        return Future.of(() -> paymentProvider.apply(charges.reduce(Charge::combine)));
    }

    private interface Clearance {

        static Clearance cleared(BigDecimal amount){
            return new Cleared(amount);
        }

        static Clearance uncleared(Charge charge, String reason){
            return new Uncleared(charge, reason);
        }
    }

    private static final class Cleared implements Clearance {
        final BigDecimal payed;

        private Cleared(BigDecimal payed) {
            this.payed = payed;
        }

        @Override
        public String toString() {
            return "Cleared{" +
                    "payed=" + payed +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cleared cleared = (Cleared) o;
            return Objects.equals(payed, cleared.payed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(payed);
        }
    }

    private static final class Uncleared implements Clearance {
        final Charge charge;
        final String reason;

        private Uncleared(Charge charge, String reason) {
            this.charge = charge;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "Uncleared{" +
                    "charge=" + charge +
                    ", reason='" + reason + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Uncleared uncleared = (Uncleared) o;
            return Objects.equals(charge, uncleared.charge) &&
                    Objects.equals(reason, uncleared.reason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(charge, reason);
        }
    }

    // Exercise 6
    // In Exercise 4 we used Future to represent the result of a future computation.
    // Does this violate purity? Discuss this with your pair.

    // Yes, it is. Type future represents any result of the action. That is, Future contains the result of the thing
    // which happens in the future.
}
