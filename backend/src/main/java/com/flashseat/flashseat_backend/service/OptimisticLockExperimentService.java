package com.flashseat.flashseat_backend.service;

import com.flashseat.flashseat_backend.entity.Seat;
import com.flashseat.flashseat_backend.entity.SeatStatus;
import com.flashseat.flashseat_backend.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;

@Service
public class OptimisticLockExperimentService {

    private final SeatRepository seatRepository;

    public OptimisticLockExperimentService(SeatRepository seatRepository){
        this.seatRepository = seatRepository;
    }

    @Transactional
    public void reserveSeat(
            Long seatId,
            CountDownLatch readySignal,
            CountDownLatch continueSignal
    ) throws InterruptedException {

        Seat seat = seatRepository.findById(seatId).orElseThrow();

        System.out.println(
                Thread.currentThread().getName()
                        + " READ seatId = " + seatId
                        + " status = " + seat.getStatus()
                        + " version = " + seat.getVersion()
        );

        readySignal.countDown();

        continueSignal.await();

        seat.setStatus(SeatStatus.RESERVED);
        System.out.println(
                Thread.currentThread().getName()
                        + " UPDATE seatId=" + seatId
                        + " version=" + seat.getVersion()
                        + " -> RESERVED"
        );
    }
}
