package it.uniroma2.ISW2.Pepe.Federico;

import java.time.LocalDateTime;
import java.util.List;

public record JiraTicket(String key,
                         LocalDateTime created,
                         LocalDateTime resolutionDate,
                         List<String> affectedVersions,
                         List<String> fixVersions) {}
