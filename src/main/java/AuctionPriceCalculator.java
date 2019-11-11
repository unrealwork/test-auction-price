import java.util.List;

public class AuctionPriceCalculator {
    public double calc(final Book book) {
        long[] totalBids = totalBids(book.getBids(), false);
        long[] totalAsks = totalBids(book.getAsks(), true);
        double[] bidPrices = book.getBids().stream().mapToDouble(Order::getPrice).toArray();
        double[] askPrices = book.getAsks().stream().mapToDouble(Order::getPrice).toArray();
        for (int i = 0; i < bidPrices.length; i++) {
            int lastMatchedAskIndex = -1;
            while (lastMatchedAskIndex < askPrices.length && askPrices[lastMatchedAskIndex + 1] < bidPrices[i]) {
                lastMatchedAskIndex++;
            }
            if (lastMatchedAskIndex < askPrices.length) {
                long matchedVol = Math.min(totalBids[i], totalAsks[lastMatchedAskIndex]);
                long surplus = totalBids[i] - totalAsks[lastMatchedAskIndex];
                System.out.printf("total bid = %5d bid = %f ask = %f total ask = %5d matched vol. = %d surplus = %d %n",
                        totalBids[i], bidPrices[i], askPrices[lastMatchedAskIndex], totalAsks[lastMatchedAskIndex],
                        matchedVol, surplus);
            }
        }
        return 0d;
    }

    private static long[] totalBids(List<Order> bids, boolean asc) {
        long[] result = new long[bids.size()];
        int start = asc ? bids.size() - 1 : 0;
        int end = asc ? -1 : bids.size();
        int step = asc ? -1 : 1;
        for (int i = start; i != end; i += step) {
            int count = bids.get(i).getCount();
            result[i] = i == start ? count : result[i - step] + count;
        }
        return result;
    }
}
