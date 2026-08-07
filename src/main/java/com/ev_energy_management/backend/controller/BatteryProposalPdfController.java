package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.client.RulDiagnosisPdfClient;
import com.ev_energy_management.backend.dto.battery.BatteryProposalPdfRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 웹(BatteryDiagnosis.jsx "배터리 매도 제안서" 탭)이 화면에 이미 표시한 값을 그대로
// 받아 rul-diagnosis 서비스(reportlab)로 PDF를 렌더링해 그대로 스트리밍한다.
// 이전엔 프론트에서 화면을 스크린샷 찍어 PDF로 저장했는데(html2canvas), 그건 진짜 문서
// 양식이 아니었고 텍스트도 없는 이미지 한 장짜리였다 - 이제 정식 문서 양식으로 나간다.
@RestController
@RequestMapping("/api/battery-proposals")
public class BatteryProposalPdfController {

    private final RulDiagnosisPdfClient rulDiagnosisPdfClient;

    public BatteryProposalPdfController(RulDiagnosisPdfClient rulDiagnosisPdfClient) {
        this.rulDiagnosisPdfClient = rulDiagnosisPdfClient;
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> renderProposalPdf(@RequestBody BatteryProposalPdfRequest request) {
        byte[] pdf = rulDiagnosisPdfClient.renderProposalPdf(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "battery_proposal.pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
