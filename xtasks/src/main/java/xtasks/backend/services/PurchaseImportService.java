package xtasks.backend.services;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xtasks.backend.entities.ImportedFile;
import xtasks.backend.entities.P_item;
import xtasks.backend.repositories.ImportedFileRepository;
import xtasks.backend.services.exceptions.GenericServiceException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class PurchaseImportService {
    @Autowired private ImportedFileRepository importedFiles;
    @Autowired private PastPurchasesService history;
    @Autowired private ItemsService catalogue;

    /** Returns false for bytes already successfully imported into this database. */
    @Transactional(rollbackFor = Exception.class)
    public boolean importCsv(File file) throws IOException, GenericServiceException {
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new IllegalArgumentException("Only CSV files are supported.");
        byte[] contents = Files.readAllBytes(file.toPath());
        String digest;
        try {
            StringBuilder hex = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(contents))
                hex.append(String.format("%02x", value & 255));
            digest = hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
        if (importedFiles.existsById(digest)) return false;
        List<P_item> rows;
        try (Reader reader = new StringReader(new String(contents, StandardCharsets.UTF_8))) {
            rows = new CsvToBeanBuilder<P_item>(reader).withType(P_item.class)
                    .withSeparator(';').withIgnoreLeadingWhiteSpace(true).build().parse();
        }
        if (rows.isEmpty()) throw new IllegalArgumentException("CSV contains no purchase records.");
        for (P_item row : rows) {
            catalogue.validateName(row.getName());
            catalogue.validateUnit(row.getUnit());
            catalogue.validateCategory(row.getCategory());
            if (row.getAddDate() == null || row.getQuantity() <= 0)
                throw new IllegalArgumentException("Each CSV row needs a date and a positive quantity.");
        }
        // Claim the content hash in the same transaction as all catalogue/history changes.
        // A failed or concurrent duplicate import rolls back without partial purchases.
        importedFiles.saveAndFlush(new ImportedFile(digest));
        for (P_item row : rows) {
            boolean exists = catalogue.items().stream().anyMatch(item ->
                    row.getName().equals(item.getName()) && row.getUnit().equals(item.getUnit())
                            && row.getCategory().equals(item.getCategory()));
            if (!exists) catalogue.create(row.getUnit(), row.getName(), "/images/standard.jpg", row.getCategory());
        }
        history.addItemsToPastPurchases(rows);
        return true;
    }
}
