package com.gnovoa.football.sim;

import java.util.concurrent.ThreadLocalRandom;

public final class LocalRandomSource implements RandomSource {
    @Override public int nextIntInclusive(int fromInclusive, int toInclusive) {
        return ThreadLocalRandom.current().nextInt(fromInclusive, toInclusive + 1);
    }
    @Override public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
