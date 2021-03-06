package com.atlassian.bamboo.plugins.jmeter_aggregator;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * A sampler that aggregates data as it receives samples
 */
public class SamplerImpl implements Sampler {
    private static final long ONE_MINUTE = 60 * 1000;
    private final String label;
    private final PrimitiveLongList samples;
    private int successCount = 0;
    private long startTime = Long.MAX_VALUE;
    private long finishTime = 0;
    private long maxValue = 0;
    private long minValue = Long.MAX_VALUE;
    private long totalValue = 0;
    private double meanValue = 0;
    private double varianceTimesCount = 0;
    private long medianValue = -1;
    private long ninetyPercentValue = -1;
    private long throughput = -1;
    private int percentSuccess = -1;
    private long totalTime = -1;
    private int count = 0;

    public SamplerImpl(final String label) {
        this.label = label;
        this.samples = new PrimitiveLongList();
    }

    public void addSample(final long startTime, final long value, final boolean pass) {
        count++;
        if (pass) {
            successCount++;
        }
        samples.add(value);
        if (startTime < this.startTime) {
            this.startTime = startTime;
        }
        if (startTime > this.finishTime) {
            this.finishTime = startTime;
        }
        if (value > maxValue) {
            maxValue = value;
        }
        if (value < minValue) {
            minValue = value;
        }
        totalValue += value;

        // Mean and variance
        final double delta = value - meanValue;
        meanValue += delta / count;
        varianceTimesCount += delta * (value - meanValue);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getSuccessCount() {
        return successCount;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getFinishTime() {
        return finishTime;
    }

    @Override
    public long getMaxValue() {
        return maxValue;
    }

    @Override
    public long getMinValue() {
        return minValue;
    }

    @Override
    public long getTotalValue() {
        return totalValue;
    }

    @Override
    public long getMedianValue() {
        if (medianValue == -1) {
            medianValue = select(samples.getPrimitiveArray(), count / 2, 0, count);
        }
        return medianValue;
    }

    @Override
    public long getNinetyPercentValue() {
        if (ninetyPercentValue == -1) {
            final int ninetyPercent = (int)Math.floor(count * 0.9);
            ninetyPercentValue = select(samples.getPrimitiveArray(), ninetyPercent, 0, count);
        }
        return ninetyPercentValue;
    }

    /**
     * Get the number of requests handled per second
     */
    @Override
    public long getThroughput() {
        if (throughput == -1) {
            if (getTotalTime() > 0) {
                throughput = (long)(count / ((double)getTotalTime() / ONE_MINUTE));
            } else {
                throughput = 0;
            }
        }
        return throughput;
    }

    @Override
    public long getAverageValue() {
        if (meanValue == -1) {
            if (count != 0) {
                meanValue = totalValue / count;
            } else {
                meanValue = 0;
            }
        }
        return (long)meanValue;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public int getPercentSuccess() {
        if (percentSuccess == -1) {
            if (count != 0) {
                percentSuccess = successCount * 100 / count;
            } else {
                percentSuccess = 0;
            }
        }
        return percentSuccess;
    }

    @Override
    public long getTotalTime() {
        if (totalTime == -1) {
            totalTime = finishTime - startTime;
        }
        return totalTime;
    }

    @Override
    public long getStandardDeviation() {
        if (count > 0) {
            return (long)Math.sqrt(varianceTimesCount / count);
        }
        return 0;
    }

    /**
     * Linear time selection algorithm
     * <p/>
     * For an explanation, see <a
     * href="http://en.wikipedia.org/wiki/Selection_algorithm">http://en.wikipedia.org/wiki/Selection_algorithm</a>.
     * 
     * @param list The list to sort
     * @param k Which element to get, that is, get the kth largest element, starting from 0
     * @param left The index of the first element in list, inclusive
     * @param right The index of the last element in list, exclusive
     * @return The kth largest element
     */
    static long select(final long[] list, final int k, final int left, final int right) {
        final int length = right - left;
        if (length < 1) {
            return 0;
        }
        if (length <= 5) {
            Arrays.sort(list, left, right);
            return list[k];
        }
        final int ml = (int)Math.ceil(length / 5.0);
        final long[] medians = new long[ml];
        for (int i = 0; i < ml; i++) {
            final int l = left + i * 5;
            int r = l + 5;
            if (r > right) {
                r = right;
            }
            Arrays.sort(list, l, r);
            medians[i] = list[l + ((r - l) / 2)];
        }
        final int pk = (int)Math.ceil(length / 10.0);
        final long pivotValue = select(medians, pk, 0, ml);
        final int pivotNewIndex = partition(list, left, right, pivotValue, k);
        if (k == pivotNewIndex) {
            return pivotValue;
        }
        if (k < pivotNewIndex) {
            return select(list, k, left, pivotNewIndex);
        } else {
            return select(list, k, pivotNewIndex, right);
        }
    }

    /**
     * Partition the list around the pivotValue, returning the index of the partition point.
     * <p/>
     * Let the return value be i, the contract for this method is list[left:i] <= pivotValue, and list[i:right] >= pivotValue.
     * <p/>
     * 
     * @param list The list to partition
     * @param left The first index in the list to include
     * @param right The last index (exclusive) to include
     * @param pivotValue The value to partition around
     * @param preferredIndex The preferred index to return
     * @return The pivotIndex This will be as close to preferredIndex as possible
     */
    static int partition(final long[] list, final int left, final int right, final long pivotValue, final int preferredIndex) {
        int storeIndex = left;
        int storeRight = right;
        for (int i = left; i < storeRight; i++) {
            final long value = list[i];
            if (value < pivotValue) {
                final long tmp = list[storeIndex];
                list[storeIndex] = list[i];
                list[i] = tmp;
                storeIndex++;
            } else if (value == pivotValue) {
                storeRight--;
                list[i] = list[storeRight];
                i--;
            }
        }
        for (int i = 0; (i < right - storeRight) && (i < storeRight - storeIndex); i++) {
            list[right - 1 - i] = list[storeIndex + i];
        }
        for (int i = 0; i < right - storeRight; i++) {
            list[storeIndex + i] = pivotValue;
        }
        if (preferredIndex < storeIndex) {
            return storeIndex;
        } else if (preferredIndex >= storeIndex + (right - storeRight)) {
            return storeIndex + (right - storeRight) - 1;
        } else {
            return preferredIndex;
        }
    }

    /**
     * A growable array of longs, this stores longs as long, rather than Long, so is more memory efficient. The only
     * supported method for adding/changing data is the add() method.
     */
    private class PrimitiveLongList extends AbstractList<Long> {
        private long[] data;
        private int size;

        public PrimitiveLongList() {
            data = new long[10];
            size = 0;
        }

        @Override
        public Long get(final int i) {
            if (i >= size) {
                throw new IndexOutOfBoundsException(Integer.toString(i));
            }
            return data[i];
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean add(final Long element) {
            if (data.length == size) {
                final long[] oldData = data;
                // This is the heuristic as used by ArrayList
                data = new long[(oldData.length * 3) / 2 + 1];
                System.arraycopy(oldData, 0, data, 0, size);
            }
            data[size] = element;
            size++;
            return true;
        }

        /**
         * Returns the array that backs this list. Note, this array may be longer than size(), so do not use
         * array.length.
         * 
         * @return The array that backs this list
         */
        public long[] getPrimitiveArray() {
            return data;
        }
    }

}
