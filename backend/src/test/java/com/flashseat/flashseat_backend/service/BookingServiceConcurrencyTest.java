package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.dto.BookingCreateRequest;
import com.flashseat.flashseat_backend.entity.*;
import com.flashseat.flashseat_backend.exception.SeatNotAvailableException;
import com.flashseat.flashseat_backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingServiceConcurrencyTest {

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private OptimisticLockExperimentService optimisticLockExperimentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private Event event;
    private Seat seat;

    private User createUser(String email) {
        OffsetDateTime now = OffsetDateTime.now();

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("test_password"))
                .role(UserRole.USER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userRepository.save(user);
    }

    private Event createEvent() {
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);
        Event event = Event.builder()
                .name("Concurrency Test Event")
                .venue("Test Venue")
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return eventRepository.save(event);
    }

    private Seat createSeat(Event event) {
        Seat seat = Seat.builder()
                .event(event)
                .seatNumber("A1")
                .price(new BigDecimal("500.00"))
                .status(SeatStatus.AVAILABLE)
                .build();

        return seatRepository.save(seat);
    }

    private Seat createSeat(Event event, String seatNumber) {
        Seat seat = Seat.builder()
                .event(event)
                .seatNumber(seatNumber)
                .price(new BigDecimal("500.00"))
                .status(SeatStatus.AVAILABLE)
                .build();
        return seatRepository.save(seat);
    }

    private SeatStatus getSeatStatus(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow()
                .getStatus();
    }

    @Test
    void shouldAllowOnlyOneUserToBookTheSameSeat() throws Exception {

        String testId = UUID.randomUUID().toString();
        userA = createUser("user-a-" + testId + "@test.com");
        userB = createUser("user-b-" + testId + "@test.com");

        event = createEvent();
        seat = createSeat(event);

        BookingCreateRequest request = new BookingCreateRequest(List.of(seat.getId()));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Callable<Object> userABooking = () -> {
            startSignal.await();
            try {
                return bookingService.createBooking(
                        event.getId(),
                        userA.getId(),
                        request
                );
            } catch (Exception e) {
                return e;
            }
        };

        Callable<Object> userBBooking = () -> {
            startSignal.await();
            try {
                return bookingService.createBooking(
                        event.getId(),
                        userB.getId(),
                        request
                );
            } catch (Exception e) {
                return e;
            }
        };
        long bookingsBefore = bookingRepository.count();
        Future<Object> futureA = executor.submit(userABooking);
        Future<Object> futureB = executor.submit(userBBooking);

        startSignal.countDown();

        Object resultA = futureA.get();
        Object resultB = futureB.get();

        executor.shutdown();

        boolean aSucceeded = !(resultA instanceof Exception);
        boolean bSucceeded = !(resultB instanceof Exception);

        assertNotEquals(aSucceeded, bSucceeded);

        Object failedResult = aSucceeded ? resultB : resultA;

        assertEquals(
                SeatNotAvailableException.class,
                failedResult.getClass()
        );

        long bookingsAfter = bookingRepository.count();

        assertEquals(bookingsBefore+1, bookingsAfter);

        System.out.println("Booking count: " + bookingsAfter);

        Seat savedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        System.out.println("Final Seat status: " + savedSeat.getStatus());

        assertEquals(SeatStatus.RESERVED, savedSeat.getStatus());
    }

    @Test
    void shouldPreventConcurrentOverlappingMultiSeatBookings() throws Exception {

        String testId = UUID.randomUUID().toString();

        userA = createUser("multi-user-a-" + testId + "@test.com");
        userB = createUser("multi-user-b-" + testId + "@test.com");

        event = createEvent();

        Seat seatA1 = createSeat(event, "A1");
        Seat seatA2 = createSeat(event, "A2");
        Seat seatA3 = createSeat(event, "A3");

        BookingCreateRequest requestA =
                new BookingCreateRequest(
                        List.of(seatA1.getId(), seatA2.getId())
                );

        BookingCreateRequest requestB =
                new BookingCreateRequest(
                        List.of(seatA2.getId(), seatA3.getId())
                );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Callable<Object> userABooking = () -> {
            startSignal.await();

            try {
                return bookingService.createBooking(
                        event.getId(),
                        userA.getId(),
                        requestA
                );
            } catch (Exception e) {
                return e;
            }
        };

        Callable<Object> userBBooking = () -> {
            startSignal.await();

            try {
                return bookingService.createBooking(
                        event.getId(),
                        userB.getId(),
                        requestB
                );
            } catch (Exception e) {
                return e;
            }
        };

        long bookingsBefore = bookingRepository.count();

        try {
            Future<Object> futureA = executor.submit(userABooking);
            Future<Object> futureB = executor.submit(userBBooking);

            startSignal.countDown();

            Object resultA = futureA.get();
            Object resultB = futureB.get();

            boolean aSucceeded = !(resultA instanceof Exception);
            boolean bSucceeded = !(resultB instanceof Exception);

            assertNotEquals(aSucceeded, bSucceeded);

            Object failedResult = aSucceeded ? resultB : resultA;

            assertEquals(
                    SeatNotAvailableException.class,
                    failedResult.getClass()
            );

            long bookingsAfter = bookingRepository.count();

            assertEquals(bookingsBefore + 1, bookingsAfter);

//            assertEquals(
//                    SeatStatus.RESERVED,
//                    getSeatStatus(seatA1.getId())
//            );
//
//            assertEquals(
//                    SeatStatus.RESERVED,
//                    getSeatStatus(seatA2.getId())
//            );
//
//            assertEquals(
//                    SeatStatus.AVAILABLE,
//                    getSeatStatus(seatA3.getId())
//            );

            SeatStatus statusA1 = getSeatStatus(seatA1.getId());
            SeatStatus statusA2 = getSeatStatus(seatA2.getId());
            SeatStatus statusA3 = getSeatStatus(seatA3.getId());

            System.out.println("========== CONCURRENT BOOKING TEST ==========");
            System.out.println("Seat A1: " + statusA1);
            System.out.println("Seat A2: " + statusA2);
            System.out.println("Seat A3: " + statusA3);
            System.out.println("=============================================");

            boolean userABooked =
                    statusA1 == SeatStatus.RESERVED
                            && statusA2 == SeatStatus.RESERVED
                            && statusA3 == SeatStatus.AVAILABLE;

            boolean userBBooked =
                    statusA1 == SeatStatus.AVAILABLE
                            && statusA2 == SeatStatus.RESERVED
                            && statusA3 == SeatStatus.RESERVED;

            System.out.println("Result A: " + resultA);
            System.out.println("Result B: " + resultB);
            assertTrue(
                    userABooked || userBBooked,
                    "Expected either A1+A2 or A2+A3 to be reserved, but got: "
                            + "A1=" + statusA1
                            + ", A2=" + statusA2
                            + ", A3=" + statusA3
            );

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldPreventConcurrentUpdatesUsingOptimisticLocking() throws Exception {

        String testId = UUID.randomUUID().toString();

        userA = createUser("optimistic-user-a-" + testId + "@test.com");
        userB = createUser("optimistic-user-b-" + testId + "@test.com");

        event = createEvent();

        seat = createSeat(event);

        Long seatId = seat.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch readySignal = new CountDownLatch(2);
        CountDownLatch continueSignal = new CountDownLatch(1);

        Callable<Object> transactionA = () -> {
            try {
                optimisticLockExperimentService.reserveSeat(
                        seatId,
                        readySignal,
                        continueSignal
                );

                return "SUCCESS";
            } catch (Exception e) {
                return e;
            }
        };

        Callable<Object> transactionB = () -> {
            try {
                optimisticLockExperimentService.reserveSeat(
                        seatId,
                        readySignal,
                        continueSignal
                );

                return "SUCCESS";
            } catch (Exception e) {
                return e;
            }
        };

        try {

            Future<Object> futureA = executor.submit(transactionA);
            Future<Object> futureB = executor.submit(transactionB);

            /*
             * Wait until BOTH transactions have read
             * the seat before allowing either transaction
             * to continue.
             */
            assertTrue(
                    readySignal.await(10, TimeUnit.SECONDS),
                    "Both transactions should read the seat before continuing"
            );

            System.out.println(
                    "========== BOTH TRANSACTIONS READ SEAT =========="
            );

            /*
             * Now both transactions attempt to update
             * the same version.
             */
            continueSignal.countDown();

            Object resultA = futureA.get();
            Object resultB = futureB.get();

            System.out.println("Result A: " + resultA);
            System.out.println("Result B: " + resultB);

            /*
             * Exactly one transaction must succeed.
             */
            boolean aSucceeded = resultA.equals("SUCCESS");
            boolean bSucceeded = resultB.equals("SUCCESS");

            assertNotEquals(
                    aSucceeded,
                    bSucceeded,
                    "Exactly one transaction should succeed"
            );

            /*
             * The other transaction must fail because
             * its version is stale.
             */
            Object failedResult = aSucceeded ? resultB : resultA;

            assertTrue(
                    failedResult instanceof Exception,
                    "The losing transaction should throw an exception"
            );

            System.out.println(
                    "Optimistic locking exception: "
                            + failedResult.getClass().getName()
            );

            /*
             * Verify final database state.
             */
            Seat finalSeat = seatRepository.findById(seatId)
                    .orElseThrow();

            System.out.println(
                    "Final seat status: " + finalSeat.getStatus()
            );

            System.out.println(
                    "Final seat version: " + finalSeat.getVersion()
            );

            assertEquals(
                    SeatStatus.RESERVED,
                    finalSeat.getStatus()
            );

            assertEquals(
                    1L,
                    finalSeat.getVersion()
            );

        } finally {
            executor.shutdownNow();
        }
    }

    @AfterEach
    @Transactional
    void cleanup() {
        if (event != null) {
            List<Booking> bookings = bookingRepository.findByEventId(event.getId());

            for (Booking booking : bookings) {
                bookingSeatRepository.deleteByBookingId(booking.getId());
            }
            bookingRepository.deleteAll(bookings);

//            if (seat != null) {
//                seatRepository.deleteById(seat.getId());
//            }
            List<Seat> seats = seatRepository.findByEventId(event.getId());

            seatRepository.deleteAll(seats);

            eventRepository.deleteById(event.getId());
        }

        if (userA != null) {
            userRepository.deleteById(userA.getId());
        }
        if(userB != null) {
            userRepository.deleteById(userB.getId());
        }
    }
}
