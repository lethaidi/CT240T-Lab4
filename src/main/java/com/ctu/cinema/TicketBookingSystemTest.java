package com.ctu.cinema;

import java.util.*;
import java.util.concurrent.*;

/** Kiem thu doc lap, khong can thu vien ngoai hay bat co -ea. */
public class TicketBookingSystemTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void runScenario(boolean sameSeat) throws Exception {
        TicketBookingSystem system = new TicketBookingSystem();
        ExecutorService pool = Executors.newFixedThreadPool(100);
        CountDownLatch ready = new CountDownLatch(100);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 100; i++) {
                final int seat = sameSeat ? 1 : i % 50 + 1;
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return system.bookSeat(seat, "Test");
                }));
            }
            check(ready.await(10, TimeUnit.SECONDS), "Workers chua san sang");
            start.countDown();
            int successes = 0;
            Set<Integer> bookedSeats = new HashSet<>();
            for (int i = 0; i < futures.size(); i++) {
                if (futures.get(i).get(10, TimeUnit.SECONDS)) {
                    successes++;
                    check(bookedSeats.add(sameSeat ? 1 : i % 50 + 1), "Ghe bi dat trung");
                }
            }
            check(successes == (sameSeat ? 1 : 50), "Sai so ve thanh cong");
            check(system.getSuccessfulBookings() == successes, "Sai bo dem thanh cong");
            check(system.getFailedBookings() == 100 - successes, "Sai bo dem that bai");
            check(!system.bookSeat(0, "Invalid"), "Chap nhan ghe 0");
            check(!system.bookSeat(51, "Invalid"), "Chap nhan ghe 51");
            check(system.getSuccessfulBookings() + system.getFailedBookings() == 100,
                    "Ghe khong hop le lam thay doi thong ke");
        } finally {
            start.countDown();
            pool.shutdownNow();
            check(pool.awaitTermination(10, TimeUnit.SECONDS), "Pool khong dung");
        }
    }

    public static void main(String[] args) throws Exception {
        for (int round = 0; round < 5; round++) {
            runScenario(false);
            runScenario(true);
        }
        System.out.println("PASS: 5 vong x 2 kich ban, 100 yeu cau dong thoi/kich ban; khong de ghe.");
    }
}
