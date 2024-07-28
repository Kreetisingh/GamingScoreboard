package com.players.GamingScoreBoard.common.utility;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtils {

    public static <T> List<List<T>> partition(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList());
    }
}