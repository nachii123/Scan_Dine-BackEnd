package com.example.scan_dineCustomer.customer.service;

import com.example.scan_dineCustomer.dto.CustomerResponse;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.repo.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerImportService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ImportResult importCustomers(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        List<CustomerResponse> imported = new ArrayList<>();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        if (filename.endsWith(".csv")) {
            try (InputStream inputStream = file.getInputStream();
                 CSVParser parser = CSVParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                         CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {
                for (CSVRecord record : parser) {
                    ImportOutcome outcome = importRow(readCsvRow(record));
                    if (outcome.importedCustomer() != null) {
                        imported.add(outcome.importedCustomer());
                        if (outcome.created()) created++; else updated++;
                    } else {
                        skipped++;
                    }
                }
            }
        } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(sheet.getFirstRowNum());
                Map<Integer, String> headers = readHeaders(headerRow);
                for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    ImportOutcome outcome = importRow(readExcelRow(row, headers));
                    if (outcome.importedCustomer() != null) {
                        imported.add(outcome.importedCustomer());
                        if (outcome.created()) created++; else updated++;
                    } else {
                        skipped++;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported file type. Upload .csv, .xls, or .xlsx");
        }

        return new ImportResult(created, updated, skipped, imported);
    }

    private ImportOutcome importRow(Map<String, String> row) {
        String name = normalize(row.get("name"));
        String mobile = normalize(row.get("mobile"));
        String email = normalize(row.get("email"));
        String password = normalize(row.get("password"));

        if (!StringUtils.hasText(name) || !StringUtils.hasText(mobile)) {
            return ImportOutcome.skipped();
        }

        Customer customer = customerRepository.findByMobile(mobile)
                .or(() -> StringUtils.hasText(email) ? customerRepository.findByEmail(email) : java.util.Optional.empty())
                .orElseGet(Customer::new);

        boolean created = customer.getId() == null;
        customer.setName(name);
        customer.setMobile(mobile);
        customer.setEmail(StringUtils.hasText(email) ? email : null);
        customer.setPassword(passwordEncoder.encode(StringUtils.hasText(password) ? password : UUID.randomUUID().toString()));

        Customer saved = customerRepository.save(customer);
        return new ImportOutcome(CustomerResponse.from(saved), created);
    }

    private Map<Integer, String> readHeaders(Row headerRow) {
        Map<Integer, String> headers = new HashMap<>();
        if (headerRow == null) return headers;
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell);
            headers.put(cell.getColumnIndex(), normalizeHeader(header));
        }
        return headers;
    }

    private Map<String, String> readCsvRow(CSVRecord record) {
        Map<String, String> row = new HashMap<>();
        record.toMap().forEach((key, value) -> row.put(normalizeHeader(key), value));
        return row;
    }

    private Map<String, String> readExcelRow(Row row, Map<Integer, String> headers) {
        Map<String, String> values = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : row) {
            String header = headers.get(cell.getColumnIndex());
            if (header != null) {
                values.put(header, formatter.formatCellValue(cell));
            }
        }
        return values;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeHeader(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.trim().toLowerCase(Locale.ROOT).replace(" ", "");
    }

    public record ImportResult(int created, int updated, int skipped, List<CustomerResponse> customers) {}

    private record ImportOutcome(CustomerResponse importedCustomer, boolean created) {
        static ImportOutcome skipped() {
            return new ImportOutcome(null, false);
        }
    }
}
