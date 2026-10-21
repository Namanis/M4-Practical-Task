package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class ReportExportService {
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void writeReport(Path outputPath, ShelterReportData reportData) {
        log.info("Writing report to {}", outputPath);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.println("Generated: " + LocalDateTime.now(ZoneId.of("Europe/Vilnius")).format(GENERATED_FORMAT));
            writer.println("Imported: " + reportData.importResult().allAnimals().size());
            writer.println("Skipped: " + reportData.importResult().skippedRows());
            writer.println("Unique species: " + reportData.uniqueSpecies());
            writer.println();

            for (String species : reportData.uniqueSpecies()) {
                writer.print(species + ": " + reportData.totalCountBySpecies().get(species) + " total:");
                writer.print(" Vaccinated = " + reportData.vaccinatedCountBySpecies().get(species));
                writer.println(" UNVaccinated = " + reportData.unvaccinatedCountBySpecies().get(species));
            }
            writer.println();

            writer.println("Oldest animal per species:");
            for (String species : reportData.uniqueSpecies()) {
                Animal oldest = reportData.oldestAnimalBySpecies().get(species);
                writer.println(species + ": " + oldest.getName() + " -> " + oldest.getAge().orElseThrow());
            }
            writer.println();

            List<String> needingVetInput = reportData.animalsNeedingVetInput();
            writer.println("Animals needing vet input: " + needingVetInput);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write report to " + outputPath, e);
        }

        log.info("Report written to {}", outputPath);
    }
}
