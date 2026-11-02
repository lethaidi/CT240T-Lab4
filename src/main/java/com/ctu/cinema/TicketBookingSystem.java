package com.ctu.cinema;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Bai 1: kiem tra va cap nhat ghe trong cung mot mien toi han. */
public class TicketBookingSystem {
    private static final int TOTAL_SEATS = 50;
    private final boolean[] seats = new boolean[TOTAL_SEATS + 1];
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger successfulBookings = new AtomicInteger();
    private final AtomicInteger failedBookings = new AtomicInteger();

    public boolean bookSeat(int seatNumber, String customerName) {
        if (seatNumber < 1 || seatNumber > TOTAL_SEATS) {
            System.err.println("Ghe khong hop le: " + seatNumber);
            return false;
        }
        lock.lock();
        try {
            if (seats[seatNumber]) {
                failedBookings.incrementAndGet();
                System.out.printf("[That bai] %s: ghe #%d da bi chiem.%n", customerName, seatNumber);
                return false;
            }
            // Do tre gia lap; neu bi ngat thi khong thay doi trang thai ghe.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CancellationException("Yeu cau dat ve bi ngat");
            }
            seats[seatNumber] = true;
            successfulBookings.incrementAndGet();
            System.out.printf("[Thanh cong] %s: ghe #%d.%n", customerName, seatNumber);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int getSuccessfulBookings() { return successfulBookings.get(); }
    public int getFailedBookings() { return failedBookings.get(); }

    public static void main(String[] args) throws Exception {
        TicketBookingSystem system = new TicketBookingSystem();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 1; i <= 100; i++) {
                final int customerId = i;
                final int targetSeat = (i % TOTAL_SEATS) + 1;
                results.add(executor.submit(() -> system.bookSeat(targetSeat, "Khach hang #" + customerId)));
            }
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Qua thoi gian cho xu ly");
            }
            for (Future<Boolean> result : results) result.get();
            System.out.println("=== THONG KE THREAD-SAFE ===");
            System.out.println("THANH CONG: " + system.getSuccessfulBookings());
            System.out.println("THAT BAI: " + system.getFailedBookings());
            System.out.println("TONG: " + (system.getSuccessfulBookings() + system.getFailedBookings()));
            if (system.getSuccessfulBookings() != 50 || system.getFailedBookings() != 50) {
                throw new AssertionError("Thong ke khong dung 50/50");
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
