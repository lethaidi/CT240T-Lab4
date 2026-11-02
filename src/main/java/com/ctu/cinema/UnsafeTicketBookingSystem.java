package com.ctu.cinema;

import java.util.concurrent.*;

/** Co tinh khong an toan luong, chi de quan sat race condition. */
public class UnsafeTicketBookingSystem {
    private final boolean[] seats = new boolean[51];
    private int successfulBookings;
    private int failedBookings;

    public boolean bookSeat(int seatNumber, String customerName) {
        if (seatNumber < 1 || seatNumber > 50) return false;
        if (!seats[seatNumber]) {
            try { Thread.sleep(10); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CancellationException();
            }
            seats[seatNumber] = true;
            successfulBookings++;
            System.out.printf("[Thanh cong UNSAFE] %s: ghe #%d.%n", customerName, seatNumber);
            return true;
        }
        failedBookings++;
        return false;
    }

    public static void main(String[] args) throws Exception {
        boolean contention = args.length > 0 && args[0].equals("--contention");
        UnsafeTicketBookingSystem system = new UnsafeTicketBookingSystem();
        ExecutorService pool = Executors.newFixedThreadPool(contention ? 100 : 10);
        CountDownLatch start = new CountDownLatch(1);
        java.util.List<Future<Boolean>> results = new java.util.ArrayList<>();
        try {
            for (int i = 1; i <= 100; i++) {
                final int id = i;
                final int seat = contention ? 1 : (i % 50) + 1;
                results.add(pool.submit(() -> {
                    start.await();
                    return system.bookSeat(seat, "Khach hang #" + id);
                }));
            }
            start.countDown();
            pool.shutdown();
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout");
            int returnedSuccesses = 0;
            for (Future<Boolean> result : results) if (result.get()) returnedSuccesses++;
            System.out.printf("UNSAFE: thanh cong=%d, that bai=%d, tong=%d%n",
                    system.successfulBookings, system.failedBookings,
                    system.successfulBookings + system.failedBookings);
            System.out.println("So yeu cau thuc su tra ve true: " + returnedSuccesses);
        } finally { pool.shutdownNow(); }
    }
}
