package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CsvImportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            //Header not used warning fix
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("File {} is empty", inputPath);
                return new ImportResult(allAnimals, 0);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                Animal animal = parseLine(line);                       // CHANGED (1)
                if (animal == null) {
                    skipped++;
                } else {
                    allAnimals.add(animal);
                    imported++;
                }
            }
        } catch (IOException e) {
            log.error("Failed to read {}", inputPath, e);
        }
        log.info("Import finished: {} imported, {} skipped", imported, skipped);
        return new ImportResult(allAnimals, skipped);
    }

    private Animal parseLine(String line) {
        String[] fields = line.split(",");
        if (fields.length < 5) {
            log.warn("Skipping malformed line: {}", line);
            return null;
        }
        String name = fields[0].trim();
        String species = fields[1].trim();
        if (name.isEmpty() || species.isEmpty()) {
            log.warn("Skipping row with missing name or species: {}", line);
            return null;
        }
        String ageText = fields[2].trim();
        Optional<Integer> age = Optional.empty();
        if (!ageText.isEmpty()) {
            age = parseInteger(ageText);
            if (age.isEmpty()) {
                log.warn("Skipping row with non-numeric age '{}': {}", ageText, line);
                return null;
            }
        }
        boolean vaccinated = Boolean.parseBoolean(fields[3].trim());
        String dateText = fields[4].trim();
        LocalDate intakeDate = parseIntakeDate(dateText);
        if (intakeDate == null) {
            log.warn("Skipping row with invalid intake date '{}': {}", dateText, line);
            return null;
        }

        return new Animal(name, species, age.orElse(null), vaccinated, intakeDate);
    }

    private Optional<Integer> parseInteger(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private LocalDate parseIntakeDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
