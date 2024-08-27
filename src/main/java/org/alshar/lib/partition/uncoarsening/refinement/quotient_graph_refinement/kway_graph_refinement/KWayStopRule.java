package org.alshar.lib.partition.uncoarsening.refinement.quotient_graph_refinement.kway_graph_refinement;
import org.alshar.lib.partition.PartitionConfig;

public abstract class KWayStopRule {
    public KWayStopRule(PartitionConfig config) {}
    public KWayStopRule() {}
    public abstract void pushStatistics(int gain);
    public abstract void resetStatistics();
    public abstract boolean searchShouldStop(int minCutIdx, int curIdx, int searchLimit);
}

class KWayAdaptiveStopRule extends KWayStopRule {
    private int steps;
    private double expectedGain;
    private double expectedVariance2;
    private PartitionConfig pConfig;

    public KWayAdaptiveStopRule(PartitionConfig config) {
        super(config);
        this.steps = 0;
        this.expectedGain = 0.0;
        this.expectedVariance2 = 0.0;
        this.pConfig = config;
    }

    @Override
    public void pushStatistics(int gain) {
        // Compute expectation and variance estimators
        expectedGain *= steps;
        expectedGain += gain;
        if (steps == 0) {
            expectedVariance2 = 0.0;
        } else {
            expectedVariance2 *= (steps - 1);
            expectedVariance2 += (gain) * (gain);
        }
        steps++;

        expectedGain /= steps;
        if (steps > 1)
            expectedVariance2 /= (steps - 1);
    }

    @Override
    public void resetStatistics() {
        steps = 0;
        expectedGain = 0.0;
        expectedVariance2 = 0.0;
    }

    @Override
    public boolean searchShouldStop(int minCutIdx, int curIdx, int searchLimit) {
        return steps * expectedGain * expectedGain >
                pConfig.getKwayAdaptiveLimitsAlpha() * expectedVariance2 + pConfig.getKwayAdaptiveLimitsBeta()
                && steps != 1;
    }
}
