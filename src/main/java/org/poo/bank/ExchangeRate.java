package org.poo.bank;

import org.poo.fileio.ExchangeInput;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ExchangeRate {
    private final Map<String, Map<String, Double>> exchangeRatesGraph;

    /**
     * @param exchangeRates the array of exchange rates between currencies
     */
    public ExchangeRate(final ExchangeInput[] exchangeRates) {
        this.exchangeRatesGraph = new HashMap<>();
        for (ExchangeInput exchangeRate : exchangeRates) {
            String from = exchangeRate.getFrom();
            String to = exchangeRate.getTo();
            double rate = exchangeRate.getRate();

            // add the connection "from -> to"
            if (!this.exchangeRatesGraph.containsKey(from)) {
                this.exchangeRatesGraph.put(from, new HashMap<>());
            }
            this.exchangeRatesGraph.get(from).put(to, rate);

            // add the inverse connection "to -> from"
            if (!this.exchangeRatesGraph.containsKey(to)) {
                this.exchangeRatesGraph.put(to, new HashMap<>());
            }
            this.exchangeRatesGraph.get(to).put(from, 1.0 / rate);
        }
    }

    /**
     * @param from the currency to convert from
     * @param to the currency to convert to
     * @return the exchange rate between the two currencies
     * @throws Exception if no path is found between the two currencies
     */
    public double getExchangeRate(final String from, final String to) throws Exception {
        if (from.equals(to)) {
            return 1.0;
        }

        // BFS for finding the path
        Queue<String> queue = new LinkedList<>();
        Map<String, Double> visited = new HashMap<>();
        queue.add(from);
        visited.put(from, 1.0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentRate = visited.get(current);

            if (exchangeRatesGraph.containsKey(current)) {
                for (Map.Entry<String, Double> neighbor
                        : exchangeRatesGraph.get(current).entrySet()) {
                    String next = neighbor.getKey();
                    double rate = neighbor.getValue();

                    if (!visited.containsKey(next)) {
                        double newRate = currentRate * rate;
                        visited.put(next, newRate);

                        // if found the target currency
                        if (next.equals(to)) {
                            return newRate;
                        }

                        queue.add(next);
                    }
                }
            }
        }
        throw new Exception("No exchange rate path found between " + from + " and " + to);
    }
}
