package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.BmsTwinSampleRequest;
import com.ev_energy_management.backend.dto.FastApiTwinFrameResponse;
import com.ev_energy_management.backend.dto.FastApiTwinMeasurementResponse;
import com.ev_energy_management.backend.dto.TwinFrameDto;
import com.ev_energy_management.backend.service.TwinFrameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/twin-frames")
public class TwinFrameController {

    private final TwinFrameService twinFrameService;

    public TwinFrameController(TwinFrameService twinFrameService) {
        this.twinFrameService = twinFrameService;
    }

    @GetMapping
    public List<TwinFrameDto> getTwinFrames() {
        return twinFrameService.findAll();
    }

    @GetMapping("/{frameId}")
    public TwinFrameDto getTwinFrame(@PathVariable UUID frameId) {
        return twinFrameService.findById(frameId);
    }

    @GetMapping("/car/{carId}")
    public List<TwinFrameDto> getTwinFramesByCar(@PathVariable UUID carId) {
        return twinFrameService.findByCarId(carId);
    }

    @PostMapping
    public ResponseEntity<TwinFrameDto> createTwinFrame(@RequestBody TwinFrameDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(twinFrameService.create(request));
    }

    @PostMapping("/cars/{carId}/bms-samples")
    public ResponseEntity<FastApiTwinFrameResponse> ingestBmsSample(
            @PathVariable UUID carId,
            @RequestBody BmsTwinSampleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(twinFrameService.evaluateBmsSample(carId, request));
    }

    @GetMapping("/cars/{carId}/latest-measurement")
    public FastApiTwinMeasurementResponse getLatestMeasurement(
            @PathVariable UUID carId,
            @RequestParam(defaultValue = "10") int staleAfterSeconds
    ) {
        return twinFrameService.latestMeasurement(carId, staleAfterSeconds);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidBmsSample(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<String> handleMissingLiveTwin(HttpClientErrorException.NotFound exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No live Twin measurement is available for this vehicle");
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleFastApiFailure(RestClientException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("FastAPI twin service is unavailable");
    }

    @PutMapping("/{frameId}")
    public TwinFrameDto updateTwinFrame(@PathVariable UUID frameId, @RequestBody TwinFrameDto request) {
        return twinFrameService.update(frameId, request);
    }

    @DeleteMapping("/{frameId}")
    public ResponseEntity<Void> deleteTwinFrame(@PathVariable UUID frameId) {
        twinFrameService.delete(frameId);
        return ResponseEntity.noContent().build();
    }
}
