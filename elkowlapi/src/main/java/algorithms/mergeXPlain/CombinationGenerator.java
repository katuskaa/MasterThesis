package algorithms.mergeXPlain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombinationGenerator {

    private List<List<Integer>> combinationList = new ArrayList<>();

    public CombinationGenerator(List<Integer> list) {
        Integer[] array = new Integer[list.size()];
        list.toArray(array);

        for (int i = 0; i < list.size() - 1; i++) {
            getCombinations(array, i + 1, 0, new Integer[i + 1]);
        }
    }

    private void getCombinations(Integer[] list, int size, int startPosition, Integer[] result) {
        if (size == 0) {
            combinationList.add(new ArrayList<>(Arrays.asList(result)));
            return;
        }

        for (int position = startPosition; position <= list.length - size; position++) {
            result[result.length - size] = list[position];
            getCombinations(list, size - 1, position + 1, result);
        }
    }

    public List<List<Integer>> getCombinationList() {
        return combinationList;
    }
}