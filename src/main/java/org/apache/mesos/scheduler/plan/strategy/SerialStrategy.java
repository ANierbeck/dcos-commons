package org.apache.mesos.scheduler.plan.strategy;

import org.apache.mesos.scheduler.plan.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A SerialStrategy requires that all Elements are completed in order.
 *
 * @param <C> is the type of {@link Element}s to which the Strategy applies.
 */
public class SerialStrategy<C extends Element> extends InterruptableStrategy<C> {
    protected DependencyStrategyHelper dependencyStrategyHelper;

    @Override
    public Collection<C> getCandidates(Element<C> parentElement, Collection<String> dirtyAssets) {
        if (isInterrupted()) {
            return Collections.emptyList();
        }

       return getDependencyStrategyHelper(parentElement).getCandidates(dirtyAssets);
    }

    public StrategyGenerator<C> getGenerator() {
        return new Generator<>();
    }

    private DependencyStrategyHelper getDependencyStrategyHelper(Element Element) {
        if (dependencyStrategyHelper == null) {
            dependencyStrategyHelper = new DependencyStrategyHelper(Element);
            List<? extends Element> planElements = new LinkedList<>(Element.getChildren());
            Collections.reverse(planElements);

            for (int i = 1; i < planElements.size(); i++) {
                Element previous = planElements.get(i - 1);
                Element current = planElements.get(i);
                dependencyStrategyHelper.addDependency(previous, current);
            }
        }

        return dependencyStrategyHelper;
    }

    /**
     * This class generates Strategy objects of the appropriate type.
     *
     * @param <C> is the type of {@link Element}s to which the Strategy applies.
     */
    public static class Generator<C extends Element> implements StrategyGenerator<C> {
        @Override
        public Strategy<C> generate() {
            return new SerialStrategy<>();
        }
    }
}
