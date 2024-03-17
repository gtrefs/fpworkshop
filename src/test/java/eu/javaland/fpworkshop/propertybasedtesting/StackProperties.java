package eu.javaland.fpworkshop.propertybasedtesting;

import net.jqwik.api.*;
import net.jqwik.api.state.Action;
import net.jqwik.api.state.ActionChain;
import net.jqwik.api.state.Transformer;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.api.statistics.StatisticsReport;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Taken from the jqwik User Guide
// Adapted to just use the fluent API
public class StackProperties {

    @Example
    public void getWhatIsInserted(){
        var stack = new Stack();
        stack.push("test");
        assertThat(stack.pop()).isEqualTo("test");
    }


    @Property(tries = 20)
    public void sizeShouldBeEqualToInsertedElements(@ForAll List<String> elements){
        var stack = new Stack();
        elements.forEach(stack::push);
        assertThat(stack.size()).isEqualTo(elements.size());
    }


    // Talk about actions against a stack and what should happen
    // Push = Push element on the stack
    // Pop = Pop element from the stack
    // Clear = clear all data from the stack

    private Action<Stack> push(String element) {
        return Action.<Stack>builder()
            .describeAs(String.format("push(%s)", element))
            .justMutate(stack -> {
                Statistics.label("action").collect("push");
                int sizeBefore = stack.size();
                stack.push(element);
                assertThat(stack.isEmpty()).isFalse();
                // Size should increase by one when something is pushed
                assertThat(stack.size()).isEqualTo(sizeBefore + 1);
            });
    }

    private Action<Stack> pop() {
        return Action.<Stack>when(stack -> !stack.isEmpty())
            .describeAs("pop")
            .justMutate(stack -> {
                Statistics.label("action").collect("pop");
                int sizeBefore = stack.size();
                String topBefore = stack.top();

                String popped = stack.pop();
                assertThat(popped).isEqualTo(topBefore);
                // Size should decrease by one when something was popped
                assertThat(stack.size()).isEqualTo(sizeBefore - 1);
            });
    }

    private Action<Stack> clear() {
        return Action.just(Transformer.mutate("clear", stack -> {
            Statistics.label("action").collect("clear");
            stack.clear();
            assertThat(stack.isEmpty()).describedAs("stack is empty").isTrue();
        }));
    }

    static class ClearAction extends Action.JustMutate<Stack> {
        @Override
        public void mutate(Stack stack) {
            Statistics.label("action").collect("clear");
            stack.clear();
            assertThat(stack.isEmpty()).describedAs("stack is empty").isTrue();
        }

        @Override
        public String description() {
            return "clear";
        }
    }

    // Run actions with a chain size of 32 (default)
    @Property
    @StatisticsReport(format = Histogram.class)
    void checkMyStack(@ForAll("myStackActions") ActionChain<Stack> chain) {
        chain.run();
        Statistics.label("Chain size").collect(chain.transformations().size());
    }

    @Provide
    Arbitrary<ActionChain<Stack>> myStackActions() {
        var pushElements = Arbitraries.strings().alpha().ofLength(5);
        return pushElements.flatMap(element -> ActionChain.startWith(Stack::new)
            .withAction(push(element))
            .withAction(pop())
            .withAction(clear()));
    }


    // Invariants are things which never change over the time of the chain execution
    @Property
    void checkMyStackWithInvariant(@ForAll("myStackActions") ActionChain<Stack> chain) {
        chain
            .withInvariant("greater", stack -> assertThat(stack.size()).isGreaterThanOrEqualTo(0))
//          .withInvariant("less", stack -> assertThat(stack.size()).isLessThan(5)) // Does not hold!
            .run();
    }


    // Change stack so that it would fail after 20 elements have been added
    @Provide
    @Property(tries = 10_000)
    @StatisticsReport(format = Histogram.class)
    void checkMyStackInfinite(@ForAll("infiniteStackActions") ActionChain<Stack> chain) {
        chain.run();
        Statistics.label("Chain size").collect(chain.transformations().size());

    }

    // Infinite number of executions (we have to tell when to end the action chain)
    // End of chain action becomes less probable with the size of the chain
    @Provide
    Arbitrary<ActionChain<Stack>> infiniteStackActions() {
        var pushElements = Arbitraries.strings().alpha().ofLength(5);
        return pushElements.flatMap(element -> ActionChain.startWith(Stack::new)
            .withAction(10, push(element))
            .withAction(10, pop())
            .withAction(10, clear())
            .withAction(10, Action.just(Transformer.endOfChain()))
            .infinite());
    }


    // Randomly chose length of action chan and create corresponding amount of actions
    // Better distribution over chain length
    // Adapt weights to increase probability that a chain with 20 succeeding push or pop actions is generated
    @Provide
    Arbitrary<ActionChain<Stack>> actionChainsWithArbitraryLengths() {
        var length = Arbitraries.integers().between(1, 1000).withDistribution(RandomDistribution.uniform());
        var pushElements = Arbitraries.strings().alpha().ofLength(5);

        return length.flatMap(maxLength -> pushElements.flatMap(element -> ActionChain.startWith(Stack::new)
                .withAction(10, push(element))
                .withAction(10, pop())
                .withAction(10, clear())
                .withMaxTransformations(maxLength)));
    }

    static class Stack {
        private final List<String> elements = new ArrayList<>();

        public void push(String element) {
            elements.addFirst(element);
        }

        public String pop() {
            return elements.removeFirst();
        }

        public void clear() {
            // Wrong implementation to provoke falsification for stacks with more than 2 elements
            if (elements.size() > 2) {
         // if (elements.size() > 20) {
                elements.removeFirst();
            } else {
                elements.clear();
            }
        }

        public boolean isEmpty() {
            return elements.isEmpty();
        }

        public int size() {
            return elements.size();
        }

        public String top() {
            return elements.getFirst();
        }

        @Override
        public String toString() {
            return elements.toString();
        }
    }
}
