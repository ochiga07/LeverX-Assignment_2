package analytics;

import model.OrderResult;
import model.Product;
import model.Reservation;
import warehouse.Warehouse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Analytics {

    public static void runAnalytics(List<OrderResult> results, Warehouse warehouse) {
        System.out.println("\n" + "-".repeat(30));
        System.out.println("Analytics Report");
        System.out.println("-".repeat(30));

        printOrderStatistics(results);
        printProfitInformation(results);
        printTopThreeProducts(results, results);
        printReservationStatistics(warehouse, results);

        System.out.println("\n" + "-".repeat(30));
    }

    private static void printOrderStatistics(List<OrderResult> results) {
        long successful = results.parallelStream().filter(OrderResult::isSuccess).count();
        long failed = results.parallelStream().filter(r -> !r.isSuccess()).count();

        System.out.printf("\n----Order Statistics----%n");
        System.out.printf("Total Orders Processed: %d%n", results.size());
        System.out.printf("Successful Orders: %d%n", successful);
        System.out.printf("Failed Orders: %d%n", failed);
    }

    private static void printProfitInformation(List<OrderResult> results) {
        double totalProfit = results.parallelStream()
                .filter(OrderResult::isSuccess)
                .mapToDouble(OrderResult::getProfit)
                .sum();

        System.out.printf("\nTotal Profit: $%.2f%n", totalProfit);
    }

    private static void printTopThreeProducts(List<OrderResult> results, List<OrderResult> allResults) {
        Map<Product, Integer> productSales = allResults.parallelStream()
                .filter(OrderResult::isSuccess)
                .flatMap(r -> r.getPurchasedItems().entrySet().stream())
                .collect(Collectors.groupingByConcurrent(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));

        System.out.println("\n----Top 3 Best-Selling Products----");
        productSales.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry ->
                        System.out.printf("%s - %d units sold%n",
                                entry.getKey().name(),
                                entry.getValue())
                );
    }

    private static void printReservationStatistics(Warehouse warehouse, List<OrderResult> results) {
        System.out.println("\n-----Reservation Statistics-----");

        List<Reservation> reservations = warehouse.getReservations();
        long active = reservations.parallelStream().filter(Reservation::isActive).count();
        long cancelled = reservations.parallelStream().filter(r -> !r.isActive()).count();

        System.out.printf("Total Reservations: %d%n", reservations.size());
        System.out.printf("Active Reservations: %d%n", active);
        System.out.printf("Cancelled Reservations: %d%n", cancelled);

        printReservationPercentages(reservations, results, warehouse);
    }

    private static void printReservationPercentages(List<Reservation> reservations, List<OrderResult> results, Warehouse warehouse) {
        // Map of total sold quantities per product
        Map<Product, Integer> productSales = results.parallelStream()
                .filter(OrderResult::isSuccess)
                .flatMap(r -> r.getPurchasedItems().entrySet().stream())
                .collect(Collectors.groupingByConcurrent(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));

        // Reserved quantities per product
        Map<Product, Integer> reservedQuantities = reservations.parallelStream()
                .filter(Reservation::isActive)
                .flatMap(r -> r.getReservedItems().entrySet().stream())
                .collect(Collectors.groupingByConcurrent(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));

        System.out.println("\n-----Reservation Percentage by Product-----");
        reservedQuantities.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    Product product = entry.getKey();
                    int reservedQty = entry.getValue();
                    int soldQty = productSales.getOrDefault(product, 0);
                    int totalInteraction = reservedQty + soldQty;
                    double percent = totalInteraction > 0 ? (reservedQty * 100.0 / totalInteraction) : 0.0;

                    System.out.printf("  %s: %.2f%% reserved (%d reserved, %d sold)%n",
                            product.name(), percent, reservedQty, soldQty);
                });
    }
}
