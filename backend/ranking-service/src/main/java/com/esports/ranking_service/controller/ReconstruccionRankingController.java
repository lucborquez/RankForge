package com.esports.ranking_service.controller;
import com.esports.ranking_service.model.Ranking;
import com.esports.ranking_service.service.ReconstruccionRanking;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/rankings/torneo")
public class ReconstruccionRankingController {
    private final ReconstruccionRanking service;
    public ReconstruccionRankingController(ReconstruccionRanking service) { this.service = service; }
    @PostMapping("/{torneoId}/recalcular")
    public List<Ranking> recalcular(@PathVariable Long torneoId,
            @RequestParam(defaultValue = "false") boolean cierre) {
        return service.recalcular(torneoId, cierre);
    }
}
