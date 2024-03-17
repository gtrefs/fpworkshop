package eu.javaland.fpworkshop.propertybasedtesting;

import eu.javaland.fpworkshop.propertybasedtesting.ClockSystemProperties.Clock.ClockState.One;
import eu.javaland.fpworkshop.propertybasedtesting.ClockSystemProperties.Clock.ClockState.Zero;
import net.jqwik.api.*;
import net.jqwik.api.state.Action;
import net.jqwik.api.state.ActionChain;
import net.jqwik.api.state.ActionChainArbitrary;
import net.jqwik.api.state.Transformer;
import net.jqwik.api.statistics.Histogram;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.api.statistics.StatisticsReport;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ClockSystemProperties {

    // VARIABLE clock
    // Init == clock \in {0, 1}
    // Tick == IF clock = 0 THEN clock' = 1 ELSE clock' = 0
    // Spec == Init /\ [][Tick]_<<clock>>

    // Show in TLA+


    @Property
    public void clockMustAlwaysBe0Or1(@ForAll("ticks") ActionChain<Clock> ticks){
        ticks.withInvariant("clock must be between 0 and 1",clock -> assertThat(clock.getClockValue()).isBetween(0,1));
//        ticks.withInvariant(clock -> assertThat(clock.getClockValue()).isEqualTo(2));
        Clock end = ticks.run();
        ticks.transformations().forEach(label -> Statistics.label("action").collect(label));
        Statistics.label("value at the end").collect(end.getClockValue());
    }

    @Provide
    static Arbitrary<ActionChain<Clock>> ticks() {
        return ticksForClock(Clock::new);
    }


    private static ActionChainArbitrary<Clock> ticksForClock(Supplier<Clock> clock) {
        return ActionChain
                .startWith(clock)
                .withAction(new TickAction());
    }

    private static class TickAction extends Action.JustMutate<Clock> {

        @Override
        public void mutate(Clock clock) {
            clock.tick();
        }

        @Override
        public String description() {
            return "tick";
        }

    }















    @Property
    public void clockMustAlwaysBe0or1ForDifferentInitState(@ForAll("ticksWithDifferentInitStates") ActionChain<Clock> ticks){
        ticks.withInvariant(clock -> assertThat(clock.getClockValue()).isBetween(0,1));
        var endState = ticks.run();
        Statistics.label("value at the end").collect(endState.getClockValue());
    }

    @Provide
    static Arbitrary<ActionChain<Clock>> ticksWithDifferentInitStates() {
        return Arbitraries.integers().map(Clock::new).flatMap(clock -> ticksForClock(() -> clock));
    }







    @Property
    public void clockMustAlwaysBe0or1ForDifferentInitStateExhaustive(@ForAll("ticksWithDifferentInitStatesEx") ActionChain<Clock> ticks){
        ticks.withInvariant(clock -> assertThat(clock.getClockValue()).isBetween(0,1));
        var endState = ticks.run();
        Statistics.label("value at the end").collect(endState.getClockValue());
    }

    @Provide
    static Arbitrary<ActionChain<Clock>> ticksWithDifferentInitStatesEx(){
        var clockStates = Arbitraries.ofSuppliers(Zero::new, One::new);
        return clockStates.map(Clock::new).flatMap(clock -> ticksForClock(() -> clock));
    }









    @Property
    @StatisticsReport(format = Histogram.class)
    public void ticksWithStutterStep(@ForAll("ticksAndStutter") ActionChain<Clock> ticks){
        ticks.withInvariant(clock -> assertThat(clock.getClockValue()).isBetween(0,1));
        var endState = ticks.run();
        ticks.transformations().forEach(label -> Statistics.label("action").collect(label));
        Statistics.label("value at the end").collect(endState.getClockValue());
    }

    @Provide
    static Arbitrary<ActionChain<Clock>> ticksAndStutter(){
        var clockStates = Arbitraries.ofSuppliers(Zero::new, One::new);
        return clockStates.map(Clock::new).flatMap(clock -> ticksForClockWithStutter(() -> clock));
    }

    private static ActionChainArbitrary<Clock> ticksForClockWithStutter(Supplier<Clock> clock) {
        return ticksForClock(clock).withAction(Action.just("noop", Transformer.noop()));
    }



    public static class Clock {
        private ClockState clock;

        public Clock() {
            this(0);
        }

        public Clock(int initState){
            this(ClockState.of(initState));
        }

        public Clock(ClockState init){
            this.clock = init;
        }

        public void tick() {
            clock = switch (clock) {
                case Zero __ -> new One();
                case One __-> new Zero();
            };
        }

        public int getClockValue() {
            return clock.value();
        }

        public sealed interface ClockState {
            record Zero() implements ClockState{
                @Override
                public int value() {
                    return 0;
                }
            }
            record One() implements ClockState{
                @Override
                public int value() {
                    return 1;
                }
            }

            int value();

            static ClockState of(Integer number){
                return number == 0 ? new Zero() : new One();
            }
        }
    }
}
