package com.gnovoa.football.sim;

public interface RandomSource {
    int nextIntInclusive(int fromInclusive, int toInclusive);
    double nextDouble();
}
