package analytics;

import model.OrderResult;
import model.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Analytics {
    public static void runAnalytics(List<OrderResult> results) {
        System.out.println("\n" + "-".repeat(30));
        System.out.println("Analytics Report");
        System.out.println("-".repeat(30));

        printOrdersInformation(results);
        printProfitInformation(results);
        printTopThreeProducts(results);

        System.out.println("\n" + "-".repeat(30));
    }

    private static void printOrdersInformation(List<OrderResult> results) {
        long totalOrders = results.parallelStream()
                .filter(OrderResult::isSuccess)
                .count();

        long failedOrders = results.parallelStream()
                .filter(r -> !r.isSuccess())
                .count();

        System.out.printf("\nTotal Orders Processed: %d%n", results.size());
        System.out.printf("Successful Orders: %d%n", totalOrders);
        System.out.printf("Failed Orders: %d%n", failedOrders);
    }

    private static void printProfitInformation(List<OrderResult> results) {
        double totalProfit = results.parallelStream()
                .filter(OrderResult::isSuccess)
                .mapToDouble(OrderResult::getProfit)
                .sum();

        System.out.printf("\nTotal Profit: $%.2f%n", totalProfit);
    }

    private static void printTopThreeProducts(List<OrderResult> results){
        Map<Product, Integer> productSales = results.parallelStream()
                .filter(OrderResult::isSuccess)
                .flatMap(result -> result.getPurchasedItems().entrySet().stream())
                .collect(Collectors.groupingByConcurrent(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));

        System.out.println("\nTop 3 Best-Selling Products:");
        productSales.entrySet().parallelStream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry ->
                        System.out.printf("%s - %d units sold%n",
                                entry.getKey().name(),
                                entry.getValue())
                );
    }
}
